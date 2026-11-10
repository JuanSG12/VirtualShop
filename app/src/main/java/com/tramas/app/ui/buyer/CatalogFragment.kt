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
import com.tramas.app.data.Product
import com.tramas.app.data.Repo
import com.tramas.app.databinding.FragmentCatalogBinding
import com.tramas.app.databinding.ItemProductCardBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.bindProduct
import com.tramas.app.ui.common.show
import com.tramas.app.ui.common.snack

/** Catálogo con búsqueda por texto y filtro por categoría (chips). */
class CatalogFragment : Fragment() {

    private var _b: FragmentCatalogBinding? = null
    private val b get() = _b!!
    private var category: String? = null
    private lateinit var adapter: BindingAdapter<Product, ItemProductCardBinding>

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCatalogBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        category = arguments?.getString("category")
        adapter = BindingAdapter(ItemProductCardBinding::inflate) { vb, p ->
            vb.bindProduct(p,
                onOpen = { findNavController().navigate(R.id.productDetailFragment, bundleOf("productId" to it.id)) },
                onAdd = { Repo.addToCart(it); b.root.snack(getString(R.string.added_to_cart, it.name)) })
        }
        b.rvProducts.adapter = adapter

        (listOf<String?>(null) + Repo.categories).forEach { cat ->
            b.chipGroup.addView(Chip(requireContext()).apply {
                id = View.generateViewId()
                text = cat ?: getString(R.string.all)
                isCheckable = true
                isChecked = cat == category
                setOnClickListener { category = cat; refresh() }
            })
        }
        b.etSearch.doAfterTextChanged { refresh() }
        refresh()
    }

    private fun refresh() {
        val q = b.etSearch.text.toString().trim()
        val list = Repo.catalog().filter { (category == null || it.category == category) && it.name.contains(q, ignoreCase = true) }
        adapter.items = list
        b.tvCount.text = resources.getQuantityString(R.plurals.products_found, list.size, list.size)
        b.emptyState.show(list.isEmpty(), getString(R.string.no_results), getString(R.string.no_results_hint))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
