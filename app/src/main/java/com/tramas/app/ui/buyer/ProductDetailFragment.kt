package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentProductDetailBinding
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.distanceKm
import com.tramas.app.ui.common.kmText
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadProduct
import com.tramas.app.ui.common.snack

/** Detalle de un producto: foto, precio, vendedor, descripción, selector de cantidad y botón "Agregar al carrito". */
class ProductDetailFragment : Fragment() {

    private var _b: FragmentProductDetailBinding? = null
    private val b get() = _b!!
    private var qty = 1

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProductDetailBinding.inflate(i, c, false)
        return b.root
    }

    /** Carga el producto por su id y conecta el selector de cantidad y el botón de agregar. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = requireArguments().getString("productId").orEmpty()
        launchSafe {
            val p = Services.products.get(id)
            if (p == null) { b.root.snack(getString(R.string.product_not_found)); findNavController().popBackStack(); return@launchSafe }
            b.imgArt.loadProduct(p.imageUrl, p.category)
            b.tvCategory.text = p.category.uppercase()
            b.tvName.text = p.name
            b.tvRating.text = p.sellerName
            b.tvDistance.text = getString(R.string.distance_from_you, distanceKm(Session.location, p.lat, p.lng).kmText())
            b.tvPrice.text = p.price.cop()
            b.tvSeller.text = p.sellerName
            b.tvSellerInitial.text = p.sellerName.firstOrNull()?.toString() ?: "T"
            b.tvDescription.text = p.description.ifBlank { getString(R.string.no_description) }
            b.tvStock.text = resources.getQuantityString(R.plurals.units_available, p.stock, p.stock)

            b.btnMinus.setOnClickListener { if (qty > 1) { qty--; b.tvQty.text = qty.toString() } }
            b.btnPlus.setOnClickListener { if (qty < p.stock) { qty++; b.tvQty.text = qty.toString() } else b.root.snack(getString(R.string.max_stock)) }
            b.btnAddToCart.isEnabled = p.stock > 0
            b.btnAddToCart.setOnClickListener {
                launchSafe(b.btnAddToCart) { Services.cart.add(Session.uid, p, qty); b.root.snack(getString(R.string.added_to_cart, p.name)) }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
