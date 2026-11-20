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
import com.tramas.app.data.Services
import com.tramas.app.databinding.FragmentAdminProductsBinding
import com.tramas.app.databinding.ItemAdminProductBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadProduct
import com.tramas.app.ui.common.show
import com.tramas.app.ui.common.snack

/** Gestión de productos: moderación por pestañas (pendientes / aprobados / rechazados) y eliminación de publicaciones. */
class AdminProductsFragment : Fragment() {

    private var _b: FragmentAdminProductsBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: BindingAdapter<Product, ItemAdminProductBinding>
    private var products: List<Product> = emptyList()
    private val tabs = listOf(R.string.tab_pending to ProductStatus.PENDING, R.string.tab_approved to ProductStatus.ACTIVE, R.string.tab_rejected to ProductStatus.REJECTED)

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminProductsBinding.inflate(i, c, false)
        return b.root
    }

    /** Configura el adaptador, las pestañas de moderación y escucha los productos de Firestore. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BindingAdapter(ItemAdminProductBinding::inflate) { vb, p ->
            vb.imgArt.loadProduct(p.imageUrl, p.category)
            vb.tvName.text = p.name
            vb.tvSeller.text = getString(R.string.seller_category, p.sellerName, p.category)
            vb.tvPrice.text = p.price.cop()
            vb.tvStatus.badge(p.status.label, p.status.color)
            vb.actions.isVisible = true
            // Aprobar/Rechazar según el estado actual; los aprobados solo permiten "Rechazar" (retirar del catálogo).
            vb.btnApprove.isVisible = p.status != ProductStatus.ACTIVE
            vb.btnReject.isVisible = p.status != ProductStatus.REJECTED
            vb.btnApprove.setOnClickListener { launchSafe { Services.products.setStatus(p.id, ProductStatus.ACTIVE); b.root.snack(getString(R.string.product_approved)) } }
            vb.btnReject.setOnClickListener { launchSafe { Services.products.setStatus(p.id, ProductStatus.REJECTED); b.root.snack(getString(R.string.product_rejected)) } }
            vb.root.setOnLongClickListener {
                requireContext().confirm(R.string.delete_product_title, getString(R.string.delete_product_message, p.name), R.string.delete) {
                    launchSafe { Services.products.delete(p.id); b.root.snack(getString(R.string.product_deleted)) }
                }
                true
            }
        }
        b.list.rvList.adapter = adapter
        tabs.forEach { b.tabs.addTab(b.tabs.newTab().setText(it.first)) }
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = render()
            /** No requiere acción al deseleccionar una pestaña. */
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            /** No requiere acción al volver a tocar la pestaña activa. */
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        collectFlow(Services.products.observeAll()) { products = it.sortedByDescending { p -> p.createdAt }; render() }
    }

    /** Muestra solo los productos del estado de la pestaña seleccionada. */
    private fun render() {
        if (_b == null) return
        val status = tabs[b.tabs.selectedTabPosition.coerceAtLeast(0)].second
        val list = products.filter { it.status == status }
        adapter.items = list
        b.list.emptyState.show(list.isEmpty(), getString(R.string.no_products), getString(R.string.nothing_here))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
