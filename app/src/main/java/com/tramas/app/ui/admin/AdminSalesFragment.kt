package com.tramas.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.tramas.app.R
import com.tramas.app.data.CategorySale
import com.tramas.app.data.Order
import com.tramas.app.data.Services
import com.tramas.app.databinding.FragmentAdminSalesBinding
import com.tramas.app.databinding.ItemOrderBinding
import com.tramas.app.databinding.ItemSaleRowBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.snack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Reporte de ventas: total del periodo, ventas por categoría y transacciones recientes, calculados de las órdenes pagadas. */
class AdminSalesFragment : Fragment() {

    private var _b: FragmentAdminSalesBinding? = null
    private val b get() = _b!!
    private var orders: List<Order> = emptyList()
    private var days = 30
    private lateinit var adapter: BindingAdapter<Order, ItemOrderBinding>
    private val periods = listOf(R.string.period_7 to 7, R.string.period_30 to 30, R.string.period_year to 365)
    private val dateFormat = SimpleDateFormat("dd MMM, h:mm a", Locale("es", "CO"))

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminSalesBinding.inflate(i, c, false)
        return b.root
    }

    /** Crea los chips de periodo, el adaptador de transacciones y escucha las órdenes. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        periods.forEach { (label, d) ->
            b.chipPeriod.addView(Chip(requireContext()).apply {
                id = View.generateViewId(); setText(label); isCheckable = true; isChecked = d == days
                setOnClickListener { days = d; render() }
            })
        }
        adapter = BindingAdapter(ItemOrderBinding::inflate) { vb, o ->
            vb.tvOrderId.text = getString(R.string.order_number, o.id.take(6).uppercase())
            vb.tvMeta.text = getString(R.string.order_meta_seller, o.buyerName, dateFormat.format(Date(o.createdAt)), o.itemsCount)
            vb.tvTotal.text = o.total.cop()
            vb.tvStatus.badge(o.status.label, o.status.color)
            vb.btnAction.visibility = View.GONE
        }
        b.rvOrders.adapter = adapter
        b.btnExport.setOnClickListener { export() }
        collectFlow(Services.orders.observeAll()) { orders = it.sortedByDescending { o -> o.createdAt }; render() }
    }

    /** Filtra por periodo y recalcula total, ventas por categoría y lista de transacciones. */
    private fun render() {
        if (_b == null) return
        val since = System.currentTimeMillis() - days * 86_400_000L
        val inPeriod = orders.filter { it.createdAt >= since }
        val sales = inPeriod.filter { it.status.isSale }

        b.tvTotalSales.text = sales.sumOf { it.total }.cop()
        b.tvTrend.text = getString(R.string.sales_count, sales.size)

        val byCategory = sales.flatMap { o -> o.items }.groupBy { it.category }
            .map { (cat, items) -> CategorySale(cat.ifBlank { "Otros" }, items.sumOf { it.price * it.qty }) }
            .sortedByDescending { it.amount }
        b.containerCategories.removeAllViews()
        val max = byCategory.maxOfOrNull { it.amount }?.toDouble() ?: 1.0
        byCategory.forEach { s ->
            val row = ItemSaleRowBinding.inflate(layoutInflater, b.containerCategories, false)
            row.tvCategory.text = s.category
            row.tvAmount.text = s.amount.cop()
            row.progress.setProgressCompat((s.amount / max * 100).toInt(), true)
            b.containerCategories.addView(row.root)
        }
        adapter.items = inPeriod.take(8)
    }

    /** Genera un CSV con las órdenes del periodo y lo copia al portapapeles (sin permisos de almacenamiento). */
    private fun export() {
        val since = System.currentTimeMillis() - days * 86_400_000L
        val csv = buildString {
            appendLine("id,fecha,comprador,total,estado,pago")
            orders.filter { it.createdAt >= since }.forEach { appendLine("${it.id},${Date(it.createdAt)},${it.buyerName},${it.total},${it.status.name},${it.paymentMode}") }
        }
        val cm = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("reporte_ventas.csv", csv))
        b.root.snack(getString(R.string.report_exported))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
