package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.tramas.app.R
import com.tramas.app.data.Order
import com.tramas.app.data.Repo
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentListSimpleBinding
import com.tramas.app.databinding.ItemOrderBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.show

class BuyerOrdersFragment : Fragment() {

    private var _b: FragmentListSimpleBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListSimpleBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = BindingAdapter<Order, ItemOrderBinding>(ItemOrderBinding::inflate) { vb, o ->
            vb.tvOrderId.text = getString(R.string.order_number, o.id)
            vb.tvMeta.text = getString(R.string.order_meta_buyer, o.date, o.itemsCount)
            vb.tvTotal.text = o.total.cop()
            vb.tvStatus.badge(o.status.label, o.status.color)
            vb.btnAction.isVisible = false
        }
        b.rvList.adapter = adapter
        val mine = Repo.orders.filter { it.customer == Session.name }
        adapter.items = mine
        b.emptyState.show(mine.isEmpty(), getString(R.string.no_orders), getString(R.string.no_orders_hint))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
