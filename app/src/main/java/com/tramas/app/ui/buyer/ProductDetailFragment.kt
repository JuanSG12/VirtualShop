package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Repo
import com.tramas.app.databinding.FragmentProductDetailBinding
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.snack

class ProductDetailFragment : Fragment() {

    private var _b: FragmentProductDetailBinding? = null
    private val b get() = _b!!
    private var qty = 1

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProductDetailBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val p = Repo.product(requireArguments().getInt("productId")) ?: return findNavController().popBackStack().let { }
        b.imgArt.setImageResource(p.art)
        p.photo?.let { b.imgArt.setImageBitmap(it) }
        b.tvCategory.text = p.category.uppercase()
        b.tvName.text = p.name
        b.tvRating.text = getString(R.string.rating_reviews, "%.1f".format(p.rating).replace('.', ','), 36)
        b.tvDistance.text = getString(R.string.distance_from_you, "%.1f".format(p.distanceKm).replace('.', ','))
        b.tvPrice.text = p.price.cop()
        b.tvSeller.text = p.seller
        b.tvSellerInitial.text = p.seller.first().toString()
        b.tvDescription.text = p.description
        b.tvStock.text = resources.getQuantityString(R.plurals.units_available, p.stock, p.stock)

        b.btnMinus.setOnClickListener { if (qty > 1) { qty--; b.tvQty.text = qty.toString() } }
        b.btnPlus.setOnClickListener { if (qty < p.stock) { qty++; b.tvQty.text = qty.toString() } else b.root.snack(getString(R.string.max_stock)) }
        b.btnAddToCart.setOnClickListener {
            Repo.addToCart(p, qty)
            b.root.snack(getString(R.string.added_to_cart, p.name))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
