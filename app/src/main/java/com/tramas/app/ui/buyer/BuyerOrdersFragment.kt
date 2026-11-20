package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.tramas.app.R
import com.tramas.app.data.Order
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentListSimpleBinding
import com.tramas.app.databinding.ItemOrderBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.show
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Historial de compras del comprador, leído en tiempo real de la colección `orders`. */
class BuyerOrdersFragment : Fragment() {

    private var _b: FragmentListSimpleBinding? = null
    private val b get() = _b!!
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale("es", "CO"))

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListSimpleBinding.inflate(i, c, false)
        return b.root
    }

    /** Configura el historial y escucha las órdenes del comprador en Firestore. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BindingAdapter<Order, ItemOrderBinding>(ItemOrderBinding::inflate) { vb, o ->
            vb.tvOrderId.text = getString(R.string.order_number, o.id.take(6).uppercase())
            vb.tvMeta.text = getString(R.string.order_meta_buyer, dateFormat.format(Date(o.createdAt)), o.itemsCount)
            vb.tvTotal.text = o.total.cop()
            vb.tvStatus.badge(o.status.label, o.status.color)
            vb.btnAction.isVisible = false
        }
        b.rvList.adapter = adapter
        collectFlow(Services.orders.observeForBuyer(Session.uid)) { list ->
            val sorted = list.sortedByDescending { it.createdAt }
            adapter.items = sorted
            _b?.emptyState?.show(sorted.isEmpty(), getString(R.string.no_orders), getString(R.string.no_orders_hint))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
