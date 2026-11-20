package com.tramas.app.ui.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tramas.app.data.payment.EpaycoApi
import kotlinx.coroutines.launch
import com.tramas.app.R
import com.tramas.app.data.payment.EpaycoConfig
import com.tramas.app.databinding.ActivityEpaycoBinding
import org.json.JSONObject

/** Datos que la pasarela necesita para cobrar una orden. */
data class EpaycoRequest(
    val orderId: String,
    val description: String,
    val total: Long,
    val tax: Long,
    val buyerName: String,
    val buyerEmail: String,
)

/** Contrato para abrir el checkout de ePayco y recibir de vuelta el `ref_payco` (null si el usuario cancela). */
class EpaycoContract : ActivityResultContract<EpaycoRequest, String?>() {
    override fun createIntent(context: Context, input: EpaycoRequest): Intent =
        Intent(context, EpaycoCheckoutActivity::class.java)
            .putExtra("orderId", input.orderId).putExtra("description", input.description)
            .putExtra("total", input.total).putExtra("tax", input.tax)
            .putExtra("name", input.buyerName).putExtra("email", input.buyerEmail)

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        if (resultCode == Activity.RESULT_OK) intent?.getStringExtra("ref_payco") else null
}

/**
 * Pantalla de pago: carga el Checkout estándar de ePayco dentro de un WebView.
 * Flujo: se abre checkout.js con la llave pública -> el usuario paga -> ePayco redirige a [EpaycoConfig.RESPONSE_URL]
 * con `?ref_payco=...` -> la app intercepta esa URL, cierra la pantalla y devuelve la referencia para verificarla.
 */
class EpaycoCheckoutActivity : AppCompatActivity() {

    /** Configura el WebView y carga el checkout de ePayco. */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityEpaycoBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }

        b.web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        b.web.webViewClient = object : WebViewClient() {
            /** Intercepta la URL de respuesta de ePayco para capturar el ref_payco y cerrar la pantalla. */
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                if (url.toString().startsWith(EpaycoConfig.RESPONSE_URL)) { finishWith(url); return true }
                return false
            }
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // Plan B por si la redirección de ePayco no pasa por shouldOverrideUrlLoading.
                if (url.startsWith(EpaycoConfig.RESPONSE_URL)) { view.stopLoading(); finishWith(Uri.parse(url)); return }
                b.progress.isVisible = true
            }
            override fun onPageFinished(view: WebView, url: String) { b.progress.isVisible = false }
        }
        // v2: primero se crea la sesión de pago; v1: se abre directo con la llave pública.
        lifecycleScope.launch {
            try {
                val html = if (EpaycoConfig.useSession) sessionHtml(createSession()) else checkoutHtml()
                b.web.loadDataWithBaseURL("https://checkout.epayco.co", html, "text/html", "UTF-8", null)
            } catch (e: Exception) {
                Toast.makeText(this@EpaycoCheckoutActivity, e.message ?: getString(R.string.payment_start_error), Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    /** Checkout v2: crea la sesión de pago en ePayco con el monto de la orden y devuelve su id. */
    private suspend fun createSession(): String = EpaycoApi.createSession(
        description = intent.getStringExtra("description") ?: "Compra en Tramas",
        invoice = intent.getStringExtra("orderId").orEmpty(),
        amount = intent.getLongExtra("total", 0),
        tax = intent.getLongExtra("tax", 0),
    )

    /** Página HTML del checkout v2: abre la sesión en modo "standard" (página completa de ePayco, redirige a la URL de respuesta). */
    private fun sessionHtml(sessionId: String): String = """
        <html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
        <body style="font-family:sans-serif;text-align:center;padding:40px;color:#555">
        <p>Abriendo pasarela de pago…</p>
        <script src="https://checkout.epayco.co/checkout-v2.js"></script>
        <script>
          var checkout = ePayco.checkout.configure({ sessionId: "$sessionId", type: "standard", test: ${EpaycoConfig.testMode} });
          checkout.open();
        </script>
        </body></html>
    """.trimIndent()

    /** Devuelve a la pantalla anterior el ref_payco que ePayco puso en la URL de respuesta. */
    private fun finishWith(url: Uri) {
        val ref = url.getQueryParameter("ref_payco") ?: url.getQueryParameter("x_ref_payco")
        if (ref == null) { setResult(Activity.RESULT_CANCELED) } else setResult(Activity.RESULT_OK, Intent().putExtra("ref_payco", ref))
        finish()
    }

    /** Página HTML mínima que configura el Checkout de ePayco y lo abre automáticamente. */
    private fun checkoutHtml(): String {
        val total = intent.getLongExtra("total", 0)
        val tax = intent.getLongExtra("tax", 0)
        val data = JSONObject()
            .put("name", getString(R.string.app_name))
            .put("description", intent.getStringExtra("description") ?: "Compra en Tramas")
            .put("invoice", intent.getStringExtra("orderId"))
            .put("currency", "cop")
            .put("amount", total.toString())
            .put("tax_base", (total - tax).toString())
            .put("tax", tax.toString())
            .put("country", "co")
            .put("lang", "es")
            .put("external", "true") // pagina completa de ePayco (más estable en WebView que el modal)
            .put("response", EpaycoConfig.RESPONSE_URL)
            .put("name_billing", intent.getStringExtra("name") ?: "")
            .put("email_billing", intent.getStringExtra("email") ?: "")
        return """
            <html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
            <body style="font-family:sans-serif;text-align:center;padding:40px;color:#555">
            <p>Abriendo pasarela de pago…</p>
            <script src="https://checkout.epayco.co/checkout.js"></script>
            <script>
              var handler = ePayco.checkout.configure({ key: "${EpaycoConfig.publicKey}", test: ${EpaycoConfig.testMode} });
              handler.open($data);
            </script>
            </body></html>
        """.trimIndent()
    }
}
