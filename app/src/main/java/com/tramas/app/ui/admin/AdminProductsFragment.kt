package com.tramas.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.tramas.app.R
import com.tramas.app.data.Product
import com.tramas.app.data.ProductStatus
import com.tramas.app.data.Repo
import com.tramas.app.databinding.FragmentAdminProductsBinding
import com.tramas.app.databinding.ItemAdminProductBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.show
import com.tramas.app.ui.common.snack

/** Gestión de productos: moderación por pestañas (pendientes / aprobados / rechazados). */
class AdminProductsFragment : Fragment() {

    private var _b: FragmentAdminProductsBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: BindingAdapter<Product, ItemAdminProductBinding>
    private val tabs = listOf(R.string.tab_pending to ProductStatus.PENDING, R.string.tab_approved to ProductStatus.ACTIVE, R.string.tab_rejected to ProductStatus.REJECTED)

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminProductsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BindingAdapter(ItemAdminProductBinding::inflate) { vb, p ->
            vb.imgArt.setImageResource(p.art)
            vb.tvName.text = p.name
            vb.tvSeller.text = getString(R.string.seller_category, p.seller, p.category)
            vb.tvPrice.text = p.price.cop()
            vb.tvStatus.badge(p.status.label, p.status.color)
            vb.actions.isVisible = p.status != ProductStatus.ACTIVE
            vb.btnApprove.setOnClickListener { p.status = ProductStatus.ACTIVE; show(); b.root.snack(getString(R.string.product_approved)) }
            vb.btnReject.isVisible = p.status == ProductStatus.PENDING
            vb.btnReject.setOnClickListener { p.status = ProductStatus.REJECTED; show(); b.root.snack(getString(R.string.product_rejected)) }
        }
        b.list.rvList.adapter = adapter
        tabs.forEach { b.tabs.addTab(b.tabs.newTab().setText(it.first)) }
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = show()
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        show()
    }

    private fun show() {
        val status = tabs[b.tabs.selectedTabPosition.coerceAtLeast(0)].second
        val list = Repo.products.filter { it.status == status }
        adapter.items = list
        b.list.emptyState.show(list.isEmpty(), getString(R.string.no_products), getString(R.string.nothing_here))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
