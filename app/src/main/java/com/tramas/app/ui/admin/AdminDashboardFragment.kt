package com.tramas.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.ProductStatus
import com.tramas.app.data.Repo
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentAdminDashboardBinding
import com.tramas.app.databinding.ViewStatCardBinding

/** Dashboard principal del administrador: KPIs, gráfico semanal y alertas operativas. */
class AdminDashboardFragment : Fragment() {

    private var _b: FragmentAdminDashboardBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminDashboardBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvGreeting.text = getString(R.string.greeting, Session.name.substringBefore(' '))
        kpi(b.kpiUsers, R.drawable.ic_users, Repo.users.size.toString(), R.string.kpi_users)
        kpi(b.kpiProducts, R.drawable.ic_box, Repo.products.size.toString(), R.string.kpi_products)
        kpi(b.kpiOrders, R.drawable.ic_list, "87", R.string.kpi_orders_today)
        kpi(b.kpiSales, R.drawable.ic_trend, "$ 42,3 M", R.string.kpi_sales_month)
        b.chart.values = listOf("L" to 2.1f, "M" to 3.4f, "X" to 2.8f, "J" to 4.6f, "V" to 3.9f, "S" to 5.2f, "D" to 3.1f)

        val pending = Repo.products.count { it.status == ProductStatus.PENDING }
        val inactive = Repo.users.count { !it.active }
        b.tvPending.text = resources.getQuantityString(R.plurals.pending_products, pending, pending)
        b.tvInactive.text = resources.getQuantityString(R.plurals.inactive_users, inactive, inactive)
        b.cardPending.setOnClickListener { findNavController().navigate(R.id.adminProductsFragment) }
        b.cardInactive.setOnClickListener { findNavController().navigate(R.id.adminUsersFragment) }
    }

    private fun kpi(s: ViewStatCardBinding, icon: Int, value: String, label: Int) {
        s.imgIcon.setImageResource(icon); s.tvValue.text = value; s.tvLabel.setText(label)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
