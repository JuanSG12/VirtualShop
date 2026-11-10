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
import com.tramas.app.data.Repo
import com.tramas.app.databinding.FragmentSellerOrdersBinding
import com.tramas.app.databinding.ItemOrderBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.show

/** Historial de pedidos recibidos con pestañas por estado y avance de estado (preparar → enviar). */
class SellerOrdersFragment : Fragment() {

    private var _b: FragmentSellerOrdersBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: BindingAdapter<Order, ItemOrderBinding>
    private val groups = listOf(
        R.string.tab_new to listOf(OrderStatus.PAID),
        R.string.tab_in_process to listOf(OrderStatus.PREPARING),
        R.string.tab_shipped to listOf(OrderStatus.SHIPPED, OrderStatus.DELIVERED),
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSellerOrdersBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BindingAdapter(ItemOrderBinding::inflate) { vb, o ->
            vb.tvOrderId.text = getString(R.string.order_number, o.id)
            vb.tvMeta.text = getString(R.string.order_meta_seller, o.customer, o.city, o.itemsCount)
            vb.tvTotal.text = o.total.cop()
            vb.tvStatus.badge(o.status.label, o.status.color)
            when (o.status) {
                OrderStatus.PAID -> vb.btnAction.apply { visibility = View.VISIBLE; setText(R.string.prepare); setOnClickListener { o.status = OrderStatus.PREPARING; show() } }
                OrderStatus.PREPARING -> vb.btnAction.apply { visibility = View.VISIBLE; setText(R.string.mark_shipped); setOnClickListener { o.status = OrderStatus.SHIPPED; show() } }
                else -> vb.btnAction.visibility = View.GONE
            }
        }
        b.list.rvList.adapter = adapter
        groups.forEach { b.tabs.addTab(b.tabs.newTab().setText(it.first)) }
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = show()
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        show()
    }

    private fun show() {
        val statuses = groups[b.tabs.selectedTabPosition.coerceAtLeast(0)].second
        val list = Repo.orders.filter { it.status in statuses }
        adapter.items = list
        b.list.emptyState.show(list.isEmpty(), getString(R.string.no_orders), getString(R.string.no_orders_seller_hint))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
