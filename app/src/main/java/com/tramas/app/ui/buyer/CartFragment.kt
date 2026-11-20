package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.CartItem
import com.tramas.app.data.Pricing
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentCartBinding
import com.tramas.app.databinding.ItemCartBinding
import com.tramas.app.databinding.RowSummaryBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadProduct

/** Carrito de compras (CRUD): lista editable guardada en Firestore (cantidad / eliminar) y resumen con IVA y envío. */
class CartFragment : Fragment() {

    private var _b: FragmentCartBinding? = null
    private val b get() = _b!!

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCartBinding.inflate(i, c, false)
        return b.root
    }

    /** Configura la lista del carrito y escucha los cambios en Firestore para actualizar totales. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = Session.uid
        val adapter = BindingAdapter<CartItem, ItemCartBinding>(ItemCartBinding::inflate) { vb, item ->
            vb.imgArt.loadProduct(item.imageUrl, item.category)
            vb.tvName.text = item.name
            vb.tvUnitPrice.text = getString(R.string.price_each, item.price.cop())
            vb.tvQty.text = item.qty.toString()
            vb.tvLineTotal.text = (item.price * item.qty).cop()
            vb.btnPlus.setOnClickListener { launchSafe { Services.cart.setQty(uid, item.productId, item.qty + 1) } }
            vb.btnMinus.setOnClickListener { if (item.qty > 1) launchSafe { Services.cart.setQty(uid, item.productId, item.qty - 1) } }
            vb.btnDelete.setOnClickListener { launchSafe { Services.cart.remove(uid, item.productId) } }
        }
        b.rvCart.adapter = adapter

        collectFlow(Services.cart.observe(uid)) { items ->
            if (_b == null) return@collectFlow
            adapter.items = items
            b.emptyState.isVisible = items.isEmpty()
            b.summaryCard.isVisible = items.isNotEmpty()
            line(b.rowSubtotal, getString(R.string.subtotal), Pricing.subtotal(items))
            line(b.rowIva, getString(R.string.iva), Pricing.iva(items))
            line(b.rowShipping, getString(R.string.shipping), Pricing.shipping(items))
            line(b.rowTotal, getString(R.string.total), Pricing.total(items), bold = true)
        }
        b.btnCheckout.setOnClickListener { findNavController().navigate(R.id.checkoutFragment) }
        b.btnExplore.setOnClickListener { findNavController().navigate(R.id.catalogFragment) }
        b.btnClear.setOnClickListener {
            requireContext().confirm(R.string.clear_cart_title, getString(R.string.clear_cart_message), R.string.clear) { launchSafe { Services.cart.clear(uid) } }
        }
    }

    /** Pinta una fila del resumen (etiqueta y valor en pesos). */
    private fun line(row: RowSummaryBinding, label: String, value: Long, bold: Boolean = false) {
        row.tvLabel.text = label
        row.tvValue.text = value.cop()
        if (bold) row.tvValue.setTextAppearance(R.style.TextAppearance_Tramas_Price)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
