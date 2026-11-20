package com.tramas.app.data.payment

import com.tramas.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Configuración de ePayco. La llave pública sale de `local.properties` (EPAYCO_PUBLIC_KEY) y llega aquí vía BuildConfig. */
object EpaycoConfig {
    val publicKey: String get() = BuildConfig.EPAYCO_PUBLIC_KEY
    val testMode: Boolean get() = BuildConfig.EPAYCO_TEST

    /** Sin llave pública no hay pago real: la app usa el modo SIMULADO y lo deja registrado en la orden. */
    val isConfigured: Boolean get() = publicKey.isNotBlank()

    /** URL a la que ePayco redirige al terminar. La app la intercepta en el WebView, no necesita existir en internet. */
    const val RESPONSE_URL = "https://tramas.app/epayco/respuesta"
}

/** Resultado de consultar una transacción a ePayco. */
enum class PaymentState(val label: String) {
    APPROVED("Aprobada"), PENDING("Pendiente"), REJECTED("Rechazada")
}

/** Resultado de la verificación: referencia, estado y mensaje de ePayco. */
data class PaymentResult(val ref: String, val state: PaymentState, val message: String)

/** Cliente mínimo del servicio público de validación de ePayco. */
object EpaycoApi {

    /**
     * Confirma el estado real de una transacción consultando a ePayco por su `ref_payco`.
     * Códigos de respuesta de ePayco (x_cod_response): 1 = aceptada, 3 = pendiente, cualquier otro = rechazada/fallida/cancelada.
     */
    suspend fun verify(refPayco: String): PaymentResult = withContext(Dispatchers.IO) {
        val conn = URL("https://secure.epayco.co/validation/v1/reference/$refPayco").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream).bufferedReader().use { it.readText() }
            val data = JSONObject(body).optJSONObject("data") ?: error("Respuesta inesperada de ePayco")
            val state = when (data.optInt("x_cod_response", -1)) {
                1 -> PaymentState.APPROVED
                3 -> PaymentState.PENDING
                else -> PaymentState.REJECTED
            }
            PaymentResult(data.optString("x_ref_payco", refPayco), state, data.optString("x_response", state.label))
        } finally {
            conn.disconnect()
        }
    }
}
