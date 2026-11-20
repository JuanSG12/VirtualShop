package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.CartItem
import com.tramas.app.data.GeoPoint
import com.tramas.app.data.Order
import com.tramas.app.data.OrderItem
import com.tramas.app.data.OrderStatus
import com.tramas.app.data.Pricing
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.data.payment.EpaycoApi
import com.tramas.app.data.payment.EpaycoConfig
import com.tramas.app.data.payment.PaymentState
import com.tramas.app.databinding.DialogProcessingBinding
import com.tramas.app.databinding.FragmentCheckoutBinding
import com.tramas.app.databinding.RowSummaryBinding
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.friendly
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.payment.EpaycoContract
import com.tramas.app.ui.payment.EpaycoRequest
import kotlinx.coroutines.launch

/**
 * Pasarela de pagos. Flujo completo:
 *  1. Dirección de entrega, con botón de geolocalización (Google Fused Location + Geocoder).
 *  2. Se crea la orden en Firestore con estado "pendiente de pago".
 *  3. Se abre el checkout de ePayco (tarjeta, PSE, Nequi...). Al volver se CONSULTA el estado real de la transacción.
 *  4. Si fue aprobada: la orden pasa a "pagado", se descuenta el inventario y se vacía el carrito.
 * Si ePayco no está configurado (sin llave pública) se registra un pago SIMULADO, claramente marcado en la orden.
 */
class CheckoutFragment : Fragment() {

    private var _b: FragmentCheckoutBinding? = null
    private val b get() = _b!!
    private var cart: List<CartItem> = emptyList()
    private var point: GeoPoint? = Session.location
    private var pendingOrder: Order? = null
    private var processing: androidx.appcompat.app.AlertDialog? = null

    private val location = LocationHelper(this) { p ->
        val bind = _b ?: return@LocationHelper
        if (p == null) { bind.root.snack(getString(R.string.location_unavailable)); return@LocationHelper }
        point = p
        if (p.address.isNotBlank()) bind.etAddress.setText(p.address)
        bind.tvLocation.text = getString(R.string.location_captured, p.lat, p.lng)
    }

    /** Recibe el ref_payco que devuelve el checkout de ePayco (null si el usuario cerró la pasarela). */
    private val epayco = registerForActivityResult(EpaycoContract()) { ref -> onEpaycoResult(ref) }

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCheckoutBinding.inflate(i, c, false)
        return b.root
    }

    /** Escucha el carrito para el resumen y conecta ubicación y botón de pago. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Session.user?.address?.takeIf { it.isNotBlank() }?.let { b.etAddress.setText(it) }
        collectFlow(Services.cart.observe(Session.uid)) { items ->
            cart = items
            val bind = _b ?: return@collectFlow
            line(bind.rowSubtotal, getString(R.string.subtotal), Pricing.subtotal(items))
            line(bind.rowIva, getString(R.string.iva), Pricing.iva(items))
            line(bind.rowShipping, getString(R.string.shipping), Pricing.shipping(items))
            line(bind.rowTotal, getString(R.string.total), Pricing.total(items))
            bind.btnPay.text = getString(R.string.pay_amount, Pricing.total(items).cop())
        }
        b.btnLocation.setOnClickListener { location.request() }
        b.btnPay.setOnClickListener { if (validate()) startPayment() }
    }

    /** Valida dirección, carrito no vacío y aceptación de términos. */
    private fun validate(): Boolean {
        b.tilAddress.error = if (b.etAddress.text.toString().trim().length >= 6) null else getString(R.string.error_address)
        if (cart.isEmpty()) { b.root.snack(getString(R.string.cart_empty_error)); return false }
        if (b.tilAddress.error != null) return false
        if (!b.cbTerms.isChecked) { b.root.snack(getString(R.string.error_terms)); return false }
        return true
    }

    /** Paso 2 y 3: crea la orden pendiente y abre la pasarela (o el modo simulado si no hay llaves de ePayco). */
    private fun startPayment() = launchSafe(b.btnPay) {
        val items = cart
        val order = Order(
            buyerId = Session.uid, buyerName = Session.name, buyerEmail = Session.email,
            items = items.map { OrderItem(it.productId, it.name, it.price, it.qty, it.category, it.sellerId) },
            subtotal = Pricing.subtotal(items), iva = Pricing.iva(items), shipping = Pricing.shipping(items), total = Pricing.total(items),
            address = b.etAddress.text.toString().trim(), lat = point?.lat, lng = point?.lng,
        )
        val id = Services.orders.create(order)
        pendingOrder = order.copy(id = id)

        if (EpaycoConfig.isConfigured) {
            epayco.launch(EpaycoRequest(id, "Compra en Tramas · pedido ${id.take(6)}", order.total, order.iva, Session.name, Session.email))
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.simulated_payment_title)
                .setMessage(R.string.simulated_payment_message)
                .setPositiveButton(R.string.continue_label) { _, _ -> completePayment(pendingOrder!!, "SIM-${id.take(8)}", "Aceptada", "SIMULADO") }
                .setNegativeButton(R.string.cancel) { _, _ -> onEpaycoResult(null) }
                .setCancelable(false).show()
        }
    }

    /** Paso 3: vuelve de ePayco; se confirma el estado real de la transacción consultando a ePayco por su referencia. */
    private fun onEpaycoResult(ref: String?) {
        val order = pendingOrder ?: return
        if (ref == null) {
            viewLifecycleOwner.lifecycleScope.launch { runCatching { Services.orders.updateStatus(order.id, OrderStatus.CANCELLED) } }
            _b?.root?.snack(getString(R.string.payment_cancelled))
            return
        }
        showProcessing()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = EpaycoApi.verify(ref)
                when (result.state) {
                    PaymentState.APPROVED -> completePayment(order, result.ref, result.message, "EPAYCO")
                    PaymentState.PENDING -> {
                        Services.orders.setPayment(order.id, result.ref, result.message, "EPAYCO", OrderStatus.PENDING_PAYMENT)
                        hideProcessing(); _b?.root?.snack(getString(R.string.payment_pending))
                    }
                    PaymentState.REJECTED -> {
                        Services.orders.setPayment(order.id, result.ref, result.message, "EPAYCO", OrderStatus.REJECTED)
                        hideProcessing(); _b?.root?.snack(getString(R.string.payment_rejected, result.message))
                    }
                }
            } catch (e: Exception) {
                hideProcessing(); _b?.root?.snack(e.friendly())
            }
        }
    }

    /** Paso 4: pago aprobado -> orden "pagada", descuento de inventario, carrito vacío y mensaje de éxito. */
    private fun completePayment(order: Order, ref: String, state: String, mode: String) {
        showProcessing()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Services.orders.setPayment(order.id, ref, state, mode, OrderStatus.PAID)
                runCatching { Services.products.decrementStock(order.items) }
                Services.cart.clear(Session.uid)
                hideProcessing()
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_check)
                    .setTitle(R.string.payment_approved)
                    .setMessage(getString(R.string.payment_approved_message, order.id.take(6), order.total.cop(), ref))
                    .setPositiveButton(R.string.view_orders) { _, _ -> findNavController().navigate(R.id.buyerOrdersFragment) }
                    .setNegativeButton(R.string.keep_shopping) { _, _ -> findNavController().navigate(R.id.homeFragment) }
                    .setCancelable(false).show()
            } catch (e: Exception) {
                hideProcessing(); _b?.root?.snack(e.friendly())
            }
        }
    }

    /** Muestra el diálogo 'Procesando tu pago'. */
    private fun showProcessing() {
        if (processing == null && _b != null)
            processing = MaterialAlertDialogBuilder(requireContext()).setView(DialogProcessingBinding.inflate(layoutInflater).root).setCancelable(false).show()
    }

    /** Oculta el diálogo de procesamiento. */
    private fun hideProcessing() { processing?.dismiss(); processing = null }

    /** Pinta una fila del resumen (etiqueta y valor en pesos). */
    private fun line(row: RowSummaryBinding, label: String, value: Long) {
        row.tvLabel.text = label
        row.tvValue.text = value.cop()
    }

    override fun onDestroyView() { hideProcessing(); super.onDestroyView(); _b = null }
}
