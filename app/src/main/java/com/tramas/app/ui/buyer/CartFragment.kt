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
import com.tramas.app.data.Repo
import com.tramas.app.databinding.FragmentCartBinding
import com.tramas.app.databinding.ItemCartBinding
import com.tramas.app.databinding.RowSummaryBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.cop

/** Carrito de compras: lista editable (cantidad / eliminar) y resumen con IVA y envío. */
class CartFragment : Fragment() {

    private var _b: FragmentCartBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCartBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BindingAdapter<CartItem, ItemCartBinding>(ItemCartBinding::inflate) { vb, item ->
            val p = item.product
            vb.imgArt.setImageResource(p.art)
            vb.tvName.text = p.name
            vb.tvUnitPrice.text = getString(R.string.price_each, p.price.cop())
            vb.tvQty.text = item.qty.toString()
            vb.tvLineTotal.text = (p.price * item.qty).cop()
            vb.btnPlus.setOnClickListener { Repo.setQty(p.id, item.qty + 1) }
            vb.btnMinus.setOnClickListener { Repo.setQty(p.id, item.qty - 1) }
            vb.btnDelete.setOnClickListener { Repo.removeFromCart(p.id) }
        }
        b.rvCart.adapter = adapter

        Repo.cart.observe(viewLifecycleOwner) { items ->
            adapter.items = items
            b.emptyState.isVisible = items.isEmpty()
            b.summaryCard.isVisible = items.isNotEmpty()
            line(b.rowSubtotal, getString(R.string.subtotal), Repo.subtotal())
            line(b.rowIva, getString(R.string.iva), Repo.iva())
            line(b.rowShipping, getString(R.string.shipping), Repo.shipping())
            line(b.rowTotal, getString(R.string.total), Repo.total(), bold = true)
        }
        b.btnCheckout.setOnClickListener { findNavController().navigate(R.id.checkoutFragment) }
        b.btnExplore.setOnClickListener { findNavController().navigate(R.id.catalogFragment) }
        b.btnClear.setOnClickListener {
            requireContext().confirm(R.string.clear_cart_title, getString(R.string.clear_cart_message), R.string.clear) { Repo.clearCart() }
        }
    }

    private fun line(row: RowSummaryBinding, label: String, value: Long, bold: Boolean = false) {
        row.tvLabel.text = label
        row.tvValue.text = value.cop()
        if (bold) row.tvValue.setTextAppearance(R.style.TextAppearance_Tramas_Price)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
