package com.tramas.app.ui.seller

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Product
import com.tramas.app.data.Repo
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentProductFormBinding
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.registerCamera
import com.tramas.app.ui.common.snack

/** Crear / editar producto: foto con cámara, validaciones y ubicación por GPS. */
class ProductFormFragment : Fragment() {

    private var _b: FragmentProductFormBinding? = null
    private val b get() = _b!!
    private var photo: Bitmap? = null
    private var productId = -1

    private val camera = registerCamera { bmp -> photo = bmp; _b?.let { showPhoto(bmp) } }
    private val location = LocationHelper(this) { t -> _b?.tvLocation?.text = t }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProductFormBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        productId = requireArguments().getInt("productId", -1)
        b.ddCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Repo.categories))

        Repo.product(productId)?.let { p ->
            (activity as androidx.appcompat.app.AppCompatActivity).supportActionBar?.title = getString(R.string.edit_product)
            b.etName.setText(p.name); b.ddCategory.setText(p.category, false)
            b.etPrice.setText(p.price.toString()); b.etStock.setText(p.stock.toString()); b.etDescription.setText(p.description)
            photo = p.photo
            photo?.let { showPhoto(it) } ?: b.imgPhoto.setImageResource(p.art).also { b.photoHint.isVisible = false }
        } ?: run { (activity as androidx.appcompat.app.AppCompatActivity).supportActionBar?.title = getString(R.string.new_product) }

        b.photoBox.setOnClickListener { camera.launch(null) }
        b.btnLocation.setOnClickListener { location.request() }
        b.btnCancel.setOnClickListener { findNavController().popBackStack() }
        b.btnSave.setOnClickListener { save() }
    }

    private fun showPhoto(bmp: Bitmap) { b.imgPhoto.setImageBitmap(bmp); b.photoHint.isVisible = false }

    private fun save() {
        val name = b.etName.text.toString().trim()
        val price = b.etPrice.text.toString().toLongOrNull()
        val stock = b.etStock.text.toString().toIntOrNull()
        b.tilName.error = if (name.length >= 3) null else getString(R.string.error_product_name)
        b.tilCategory.error = if (b.ddCategory.text.isNotBlank()) null else getString(R.string.error_category)
        b.tilPrice.error = if (price != null && price > 0) null else getString(R.string.error_price)
        b.tilStock.error = if (stock != null && stock >= 0) null else getString(R.string.error_stock)
        if (listOf(b.tilName, b.tilCategory, b.tilPrice, b.tilStock).any { it.error != null }) return

        Repo.saveProduct(productId, Product(0, name, b.etDescription.text.toString().trim(), price!!, stock!!, b.ddCategory.text.toString(),
            R.drawable.art_tejido, Session.name, photo = photo))
        b.root.snack(getString(R.string.product_saved))
        findNavController().popBackStack()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
