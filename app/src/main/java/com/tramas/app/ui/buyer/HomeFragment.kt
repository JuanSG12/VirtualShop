package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.tramas.app.R
import com.tramas.app.data.Product
import com.tramas.app.data.Repo
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentHomeBinding
import com.tramas.app.databinding.ItemProductCardBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.bindProduct
import com.tramas.app.ui.common.dp
import com.tramas.app.ui.common.snack

/** Pantalla de inicio: saludo, buscador, banner, categorías, destacados y productores cercanos. */
class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvGreeting.text = getString(R.string.greeting, Session.name.substringBefore(' '))
        b.searchBar.setOnClickListener { findNavController().navigate(R.id.catalogFragment) }
        b.btnNearby.setOnClickListener { findNavController().navigate(R.id.catalogFragment) }

        Repo.categories.forEach { cat ->
            b.chipCategories.addView(Chip(requireContext()).apply {
                text = cat
                setOnClickListener { findNavController().navigate(R.id.catalogFragment, bundleOf("category" to cat)) }
            })
        }

        b.rvFeatured.adapter = productAdapter().also { it.items = Repo.catalog().sortedByDescending { p -> p.rating }.take(5) }
        b.rvNearby.adapter = productAdapter().also { it.items = Repo.catalog().sortedBy { p -> p.distanceKm }.take(5) }
    }

    private fun productAdapter() = BindingAdapter<Product, ItemProductCardBinding>(ItemProductCardBinding::inflate) { vb, p ->
        vb.root.layoutParams = RecyclerView.LayoutParams(requireContext().dp(168), ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(6, 6, 6, 6) }
        vb.bindProduct(p,
            onOpen = { findNavController().navigate(R.id.productDetailFragment, bundleOf("productId" to it.id)) },
            onAdd = { Repo.addToCart(it); b.root.snack(getString(R.string.added_to_cart, it.name)) })
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
