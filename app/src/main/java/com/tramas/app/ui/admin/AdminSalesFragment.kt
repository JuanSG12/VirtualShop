package com.tramas.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.tramas.app.R
import com.tramas.app.data.Order
import com.tramas.app.data.Repo
import com.tramas.app.databinding.FragmentAdminSalesBinding
import com.tramas.app.databinding.ItemOrderBinding
import com.tramas.app.databinding.ItemSaleRowBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.snack

/** Reporte de ventas: periodo, total, ventas por categoría y transacciones. */
class AdminSalesFragment : Fragment() {

    private var _b: FragmentAdminSalesBinding? = null
    private val b get() = _b!!
    private val periods = listOf(R.string.period_7 to 0.28, R.string.period_30 to 1.0, R.string.period_year to 11.4)

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminSalesBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        periods.forEachIndexed { idx, (label, factor) ->
            b.chipPeriod.addView(Chip(requireContext()).apply {
                id = View.generateViewId(); setText(label); isCheckable = true; isChecked = idx == 1
                setOnClickListener { render(factor) }
            })
        }
        val adapter = BindingAdapter<Order, ItemOrderBinding>(ItemOrderBinding::inflate) { vb, o ->
            vb.tvOrderId.text = getString(R.string.order_number, o.id)
            vb.tvMeta.text = getString(R.string.order_meta_seller, o.customer, o.city, o.itemsCount)
            vb.tvTotal.text = o.total.cop()
            vb.tvStatus.badge(o.status.label, o.status.color)
            vb.btnAction.visibility = View.GONE
        }
        b.rvOrders.adapter = adapter
        adapter.items = Repo.orders.take(5)
        b.btnExport.setOnClickListener { b.root.snack(getString(R.string.report_exported)) }
        render(1.0)
    }

    private fun render(factor: Double) {
        val data = Repo.salesByCategory.map { it.category to (it.amount * factor).toLong() }
        val total = data.sumOf { it.second }
        b.tvTotalSales.text = total.cop()
        b.tvTrend.text = getString(R.string.trend_up)
        b.containerCategories.removeAllViews()
        val max = data.maxOf { it.second }.toDouble()
        data.forEach { (cat, amount) ->
            val row = ItemSaleRowBinding.inflate(layoutInflater, b.containerCategories, false)
            row.tvCategory.text = cat
            row.tvAmount.text = amount.cop()
            row.progress.setProgressCompat((amount / max * 100).toInt(), true)
            b.containerCategories.addView(row.root)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
