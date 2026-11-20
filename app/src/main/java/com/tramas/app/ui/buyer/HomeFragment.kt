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
import com.tramas.app.data.Categories
import com.tramas.app.data.Product
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentHomeBinding
import com.tramas.app.databinding.ItemProductCardBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.bindProduct
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.distanceKm
import com.tramas.app.ui.common.dp
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.snack

/**
 * Inicio del comprador: saludo, buscador, banner "Cerca de ti", categorías y dos listas horizontales
 * (destacados y productores cercanos) alimentadas en tiempo real desde Firestore.
 */
class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private var products: List<Product> = emptyList()
    private lateinit var featured: BindingAdapter<Product, ItemProductCardBinding>
    private lateinit var nearby: BindingAdapter<Product, ItemProductCardBinding>

    /** "Explorar ahora": captura la ubicación del usuario para ordenar los productos por cercanía y abre el catálogo. */
    private val location = LocationHelper(this) { point ->
        if (point == null) _b?.root?.snack(getString(R.string.location_unavailable))
        else { render(); findNavController().navigate(R.id.catalogFragment) }
    }

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    /** Conecta buscador, banner, categorías y escucha el catálogo para las dos listas. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvGreeting.text = getString(R.string.greeting, Session.name.substringBefore(' '))
        b.searchBar.setOnClickListener { findNavController().navigate(R.id.catalogFragment) }
        b.btnNearby.setOnClickListener { location.request() }

        Categories.all.forEach { cat ->
            b.chipCategories.addView(Chip(requireContext()).apply {
                text = cat
                setOnClickListener { findNavController().navigate(R.id.catalogFragment, bundleOf("category" to cat)) }
            })
        }

        featured = productAdapter().also { b.rvFeatured.adapter = it }
        nearby = productAdapter().also { b.rvNearby.adapter = it }
        collectFlow(Services.products.observeCatalog()) { list -> products = list; render() }
    }

    /** Recalcula las dos listas: destacados = más recientes; cercanos = ordenados por distancia si se conoce la ubicación. */
    private fun render() {
        if (_b == null) return
        featured.items = products.sortedByDescending { it.createdAt }.take(6)
        nearby.items = products.sortedBy { distanceKm(Session.location, it.lat, it.lng) ?: Double.MAX_VALUE }.take(6)
    }

    /** Crea un adaptador de tarjetas de producto de ancho fijo para las listas horizontales. */
    private fun productAdapter() = BindingAdapter<Product, ItemProductCardBinding>(ItemProductCardBinding::inflate) { vb, p ->
        vb.root.layoutParams = RecyclerView.LayoutParams(requireContext().dp(168), ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(6, 6, 6, 6) }
        vb.bindProduct(p,
            onOpen = { findNavController().navigate(R.id.productDetailFragment, bundleOf("productId" to it.id)) },
            onAdd = { addToCart(it) })
    }

    /** Agrega un producto al carrito en Firestore y confirma con un mensaje. */
    private fun addToCart(p: Product) = launchSafe {
        Services.cart.add(Session.uid, p, 1)
        b.root.snack(getString(R.string.added_to_cart, p.name))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
