package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.tramas.app.R
import com.tramas.app.data.Categories
import com.tramas.app.data.Product
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentCatalogBinding
import com.tramas.app.databinding.ItemProductCardBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.bindProduct
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.distanceKm
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.show
import com.tramas.app.ui.common.snack

/** Catálogo completo con búsqueda por texto y filtro por categoría. Se ordena por cercanía si se conoce la ubicación. */
class CatalogFragment : Fragment() {

    private var _b: FragmentCatalogBinding? = null
    private val b get() = _b!!
    private var category: String? = null
    private var all: List<Product> = emptyList()
    private lateinit var adapter: BindingAdapter<Product, ItemProductCardBinding>

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCatalogBinding.inflate(i, c, false)
        return b.root
    }

    /** Configura la grilla, los chips de categoría, la búsqueda y escucha el catálogo. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        category = arguments?.getString("category")
        adapter = BindingAdapter(ItemProductCardBinding::inflate) { vb, p ->
            vb.bindProduct(p,
                onOpen = { findNavController().navigate(R.id.productDetailFragment, bundleOf("productId" to it.id)) },
                onAdd = { product -> launchSafe { Services.cart.add(Session.uid, product, 1); b.root.snack(getString(R.string.added_to_cart, product.name)) } })
        }
        b.rvProducts.adapter = adapter

        (listOf<String?>(null) + Categories.all).forEach { cat ->
            b.chipGroup.addView(Chip(requireContext()).apply {
                id = View.generateViewId()
                text = cat ?: getString(R.string.all)
                isCheckable = true
                isChecked = cat == category
                setOnClickListener { category = cat; refresh() }
            })
        }
        b.etSearch.doAfterTextChanged { refresh() }
        collectFlow(Services.products.observeCatalog()) { all = it; refresh() }
    }

    /** Aplica el texto de búsqueda y la categoría sobre la lista recibida de Firestore. */
    private fun refresh() {
        if (_b == null) return
        val q = b.etSearch.text.toString().trim()
        val list = all.filter { (category == null || it.category == category) && it.name.contains(q, ignoreCase = true) }
            .sortedBy { distanceKm(Session.location, it.lat, it.lng) ?: Double.MAX_VALUE }
        adapter.items = list
        b.tvCount.text = resources.getQuantityString(R.plurals.products_found, list.size, list.size)
        b.emptyState.show(list.isEmpty(), getString(R.string.no_results), getString(R.string.no_results_hint))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
