package com.tramas.app.data.payment

import android.util.Base64
import com.tramas.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Configuración de la pasarela ePayco (hoy Davipay, de Davivienda). Las llaves salen de `local.properties` y llegan por BuildConfig.
 *
 * Hay dos modos de checkout y la app soporta ambos:
 *  - **v1 (clásico):** solo con la llave pública (`EPAYCO_PUBLIC_KEY`).
 *  - **v2 (Smart Checkout):** exige crear una sesión con la llave privada (`EPAYCO_PRIVATE_KEY`, de PRUEBAS).
 * Si hay llave privada se usa v2; si solo hay pública, v1; si no hay ninguna, el pago es SIMULADO.
 */
object EpaycoConfig {
    val publicKey: String get() = BuildConfig.EPAYCO_PUBLIC_KEY
    val privateKey: String get() = BuildConfig.EPAYCO_PRIVATE_KEY
    val testMode: Boolean get() = BuildConfig.EPAYCO_TEST

    /** Sin llave pública no hay pago real: la app usa el modo SIMULADO y lo deja registrado en la orden. */
    val isConfigured: Boolean get() = publicKey.isNotBlank()

    /** True si se debe crear una sesión de pago (checkout v2), lo que requiere la llave privada. */
    val useSession: Boolean get() = publicKey.isNotBlank() && privateKey.isNotBlank()

    /** URL a la que ePayco redirige al terminar. La app la intercepta en el WebView, no necesita existir en internet. */
    const val RESPONSE_URL = "https://tramas.app/epayco/respuesta"
}

/** Resultado de consultar una transacción a ePayco. */
enum class PaymentState(val label: String) {
    APPROVED("Aprobada"), PENDING("Pendiente"), REJECTED("Rechazada")
}

data class PaymentResult(val ref: String, val state: PaymentState, val message: String)

/** Cliente mínimo de los servicios de ePayco/Davipay: crear sesión (v2) y validar una transacción. */
object EpaycoApi {

    private val HOSTS = listOf("https://apify.epayco.co", "https://api.epayco.co")

    /** Petición HTTP con JSON; devuelve (código, cuerpo). */
    private fun request(method: String, url: String, headers: Map<String, String> = emptyMap(), body: String? = null): Pair<Int, String> {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 15_000
            conn.readTimeout = 20_000
            conn.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            if (body != null) { conn.doOutput = true; conn.outputStream.use { it.write(body.toByteArray()) } }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            return code to text
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Checkout v2: crea una sesión de pago y devuelve su `sessionId`.
     * Paso 1: login con Basic(llavePública:llavePrivada) -> token. Paso 2: POST de la sesión con ese token.
     */
    suspend fun createSession(description: String, invoice: String, amount: Long, tax: Long): String = withContext(Dispatchers.IO) {
        val basic = Base64.encodeToString("${EpaycoConfig.publicKey}:${EpaycoConfig.privateKey}".toByteArray(), Base64.NO_WRAP)
        var lastError = "No se pudo contactar a ePayco"
        for (host in HOSTS) {
            try {
                val (loginCode, loginBody) = request("POST", "$host/login", mapOf("Authorization" to "Basic $basic"), "{}")
                val token = JSONObject(loginBody.ifBlank { "{}" }).optString("token")
                if (loginCode !in 200..299 || token.isBlank()) { lastError = "Llaves de ePayco inválidas (login $loginCode)"; continue }

                val session = JSONObject()
                    .put("checkout_version", "2").put("name", "Tramas").put("currency", "COP")
                    .put("amount", amount).put("description", description).put("invoice", invoice)
                    .put("lang", "ES").put("country", "CO")
                    .put("taxBase", amount - tax).put("tax", tax)
                    .put("response", EpaycoConfig.RESPONSE_URL).put("method", "GET")
                val (code, text) = request("POST", "$host/payment/session/create", mapOf("Authorization" to "Bearer $token"), session.toString())
                val data = JSONObject(text.ifBlank { "{}" }).optJSONObject("data")
                val id = data?.optString("sessionId").orEmpty()
                if (code in 200..299 && id.isNotBlank()) return@withContext id
                lastError = "ePayco no creó la sesión de pago (código $code)"
            } catch (e: Exception) {
                lastError = e.message ?: lastError
            }
        }
        error(lastError)
    }

    /**
     * Confirma el estado real de una transacción consultando a ePayco por su `ref_payco`
     * (la URL de respuesta por sí sola no es confiable). Lee x_cod_response (1 = aceptada, 3 = pendiente),
     * o el texto x_response / x_transaction_state ("Aceptada", "Pendiente"...). Si la respuesta no se entiende,
     * lanza un error en vez de aprobar: nunca se aprueba un pago sin confirmación explícita.
     */
    suspend fun verify(refPayco: String): PaymentResult = withContext(Dispatchers.IO) {
        val (_, body) = request("GET", "https://secure.epayco.co/validation/v1/reference/$refPayco")
        parseValidation(body, refPayco)
    }

    /** Interpreta la respuesta del servicio de validación (función pura, probada con varias formas de respuesta). */
    internal fun parseValidation(body: String, refPayco: String): PaymentResult {
        val root = JSONObject(body.ifBlank { "{}" })
        // Los campos pueden venir en "data" (a veces anidado) o en la raíz según la versión del servicio.
        val data = root.optJSONObject("data")?.let { d -> d.optJSONObject("data") ?: d } ?: root
        val text = data.optString("x_response").ifBlank { data.optString("x_transaction_state") }
        val code = data.optInt("x_cod_response", -1)
        val state = when {
            code == 1 || text.equals("Aceptada", true) || text.equals("Aprobada", true) -> PaymentState.APPROVED
            code == 3 || text.equals("Pendiente", true) -> PaymentState.PENDING
            code != -1 || text.isNotBlank() -> PaymentState.REJECTED
            else -> error("ePayco no confirmó la transacción todavía. Revisa en Mis pedidos más tarde.")
        }
        return PaymentResult(data.optString("x_ref_payco", refPayco).ifBlank { refPayco }, state, text.ifBlank { state.label })
    }
}
