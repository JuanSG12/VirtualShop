package com.tramas.app.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.tramas.app.R
import com.tramas.app.data.Order
import com.tramas.app.data.OrderStatus
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentSellerOrdersBinding
import com.tramas.app.databinding.ItemOrderBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.show
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pedidos recibidos por el vendedor, con pestañas por estado. Cada pedido avanza con un botón:
 * Pagado -> Preparando -> Enviado -> Entregado (el cambio se guarda en Firestore y lo ve el comprador al instante).
 */
class SellerOrdersFragment : Fragment() {

    private var _b: FragmentSellerOrdersBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: BindingAdapter<Order, ItemOrderBinding>
    private var orders: List<Order> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM, h:mm a", Locale("es", "CO"))

    /** Pestaña -> estados que muestra. */
    private val groups = listOf(
        R.string.tab_new to listOf(OrderStatus.PAID),
        R.string.tab_in_process to listOf(OrderStatus.PREPARING),
        R.string.tab_shipped to listOf(OrderStatus.SHIPPED, OrderStatus.DELIVERED),
    )

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSellerOrdersBinding.inflate(i, c, false)
        return b.root
    }

    /** Configura pestañas y adaptador, y escucha los pedidos del vendedor. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BindingAdapter(ItemOrderBinding::inflate) { vb, o ->
            vb.tvOrderId.text = getString(R.string.order_number, o.id.take(6).uppercase())
            vb.tvMeta.text = getString(R.string.order_meta_seller, o.buyerName, dateFormat.format(Date(o.createdAt)), o.itemsCount)
            vb.tvTotal.text = o.total.cop()
            vb.tvStatus.badge(o.status.label, o.status.color)
            val next = when (o.status) {
                OrderStatus.PAID -> OrderStatus.PREPARING to R.string.prepare
                OrderStatus.PREPARING -> OrderStatus.SHIPPED to R.string.mark_shipped
                OrderStatus.SHIPPED -> OrderStatus.DELIVERED to R.string.mark_delivered
                else -> null
            }
            vb.btnAction.visibility = if (next == null) View.GONE else View.VISIBLE
            if (next != null) {
                vb.btnAction.setText(next.second)
                vb.btnAction.setOnClickListener { launchSafe(vb.btnAction) { Services.orders.updateStatus(o.id, next.first) } }
            }
        }
        b.list.rvList.adapter = adapter
        groups.forEach { b.tabs.addTab(b.tabs.newTab().setText(it.first)) }
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = render()
            /** No requiere acción al deseleccionar una pestaña. */
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            /** No requiere acción al volver a tocar la pestaña activa. */
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        collectFlow(Services.orders.observeForSeller(Session.uid)) { orders = it.sortedByDescending { o -> o.createdAt }; render() }
    }

    /** Muestra los pedidos de la pestaña seleccionada. */
    private fun render() {
        if (_b == null) return
        val statuses = groups[b.tabs.selectedTabPosition.coerceAtLeast(0)].second
        val list = orders.filter { it.status in statuses }
        adapter.items = list
        b.list.emptyState.show(list.isEmpty(), getString(R.string.no_orders), getString(R.string.no_orders_seller_hint))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
