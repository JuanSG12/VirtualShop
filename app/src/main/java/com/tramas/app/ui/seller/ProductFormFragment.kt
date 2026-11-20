package com.tramas.app.ui.seller

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Categories
import com.tramas.app.data.Product
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentProductFormBinding
import com.tramas.app.ui.common.ImagePicker
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadProduct
import com.tramas.app.ui.common.snack

/**
 * Crear o editar un producto. La foto se toma con la cámara o se elige de la galería; al guardar se sube a
 * Firebase Storage y el producto (con la URL de la foto) se guarda en Firestore. Un producto nuevo queda "En revisión"
 * hasta que un administrador lo apruebe.
 */
class ProductFormFragment : Fragment() {

    private var _b: FragmentProductFormBinding? = null
    private val b get() = _b!!
    private var existing: Product? = null
    private var imageBytes: ByteArray? = null
    private var lat: Double? = null
    private var lng: Double? = null

    private val picker = ImagePicker(this) { bytes ->
        imageBytes = bytes
        _b?.let {
            it.imgPhoto.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            it.photoHint.isVisible = false
        }
    }

    private val location = LocationHelper(this) { p ->
        val bind = _b ?: return@LocationHelper
        if (p == null) { bind.root.snack(getString(R.string.location_unavailable)); return@LocationHelper }
        lat = p.lat; lng = p.lng
        bind.tvLocation.text = p.address.ifBlank { getString(R.string.location_captured, p.lat, p.lng) }
    }

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProductFormBinding.inflate(i, c, false)
        return b.root
    }

    /** Prepara el formulario (crear/editar), carga el producto existente y conecta foto, GPS y guardado. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.ddCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Categories.all))
        val bar = (activity as AppCompatActivity).supportActionBar
        val id = arguments?.getString("productId")
        bar?.title = getString(if (id == null) R.string.new_product else R.string.edit_product)

        // Valores iniciales: la ubicación del vendedor (si la guardó en su perfil).
        lat = Session.user?.lat; lng = Session.user?.lng
        if (id != null) launchSafe {
            val p = Services.products.get(id) ?: return@launchSafe
            existing = p
            b.etName.setText(p.name); b.ddCategory.setText(p.category, false)
            b.etPrice.setText(p.price.toString()); b.etStock.setText(p.stock.toString()); b.etDescription.setText(p.description)
            lat = p.lat ?: lat; lng = p.lng ?: lng
            b.imgPhoto.loadProduct(p.imageUrl, p.category)
            b.photoHint.isVisible = false
        }

        b.photoBox.setOnClickListener { picker.show() }
        b.btnLocation.setOnClickListener { location.request() }
        b.btnCancel.setOnClickListener { findNavController().popBackStack() }
        b.btnSave.setOnClickListener { save() }
    }

    /** Valida los campos y guarda el producto (sube la foto si el vendedor eligió una nueva). */
    private fun save() {
        val name = b.etName.text.toString().trim()
        val price = b.etPrice.text.toString().toLongOrNull()
        val stock = b.etStock.text.toString().toIntOrNull()
        b.tilName.error = if (name.length >= 3) null else getString(R.string.error_product_name)
        b.tilCategory.error = if (b.ddCategory.text.isNotBlank()) null else getString(R.string.error_category)
        b.tilPrice.error = if (price != null && price > 0) null else getString(R.string.error_price)
        b.tilStock.error = if (stock != null && stock >= 0) null else getString(R.string.error_stock)
        if (listOf(b.tilName, b.tilCategory, b.tilPrice, b.tilStock).any { it.error != null }) return

        val base = existing
        val product = (base ?: Product(name = name, description = "", price = 0, stock = 0, category = "", sellerId = Session.uid, sellerName = Session.name)).apply {
            this.name = name
            description = b.etDescription.text.toString().trim()
            this.price = price!!
            this.stock = stock!!
            category = b.ddCategory.text.toString()
            sellerName = Session.name
            lat = this@ProductFormFragment.lat
            lng = this@ProductFormFragment.lng
        }
        launchSafe(b.btnSave, onDone = {
            _b?.root?.snack(getString(if (base == null) R.string.product_saved else R.string.product_updated))
            findNavController().popBackStack()
        }) { Services.products.save(product, imageBytes) }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
