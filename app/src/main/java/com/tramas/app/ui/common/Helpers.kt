package com.tramas.app.ui.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Product
import com.tramas.app.databinding.ItemProductCardBinding
import com.tramas.app.databinding.ViewEmptyStateBinding

/** Llena la tarjeta de producto del catálogo y conecta sus acciones. */
fun ItemProductCardBinding.bindProduct(p: Product, onOpen: (Product) -> Unit, onAdd: (Product) -> Unit) {
    imgArt.setImageResource(p.art)
    p.photo?.let { imgArt.setImageBitmap(it) }
    tvName.text = p.name
    tvRating.text = "%.1f · %.1f km".format(p.rating, p.distanceKm).replace('.', ',')
    tvPrice.text = p.price.cop()
    root.setOnClickListener { onOpen(p) }
    btnAdd.setOnClickListener { onAdd(p) }
}

fun ViewEmptyStateBinding.show(visible: Boolean, title: String, text: String) {
    root.isVisible = visible
    tvEmptyTitle.text = title
    tvEmptyText.text = text
}

/** Dialog de confirmación estándar para acciones destructivas. */
fun Context.confirm(title: Int, message: String, positive: Int, onYes: () -> Unit) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title).setMessage(message)
        .setPositiveButton(positive) { _, _ -> onYes() }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

/** Cámara: abre la app de cámara del sistema y devuelve una miniatura (no requiere permiso CAMERA). */
fun Fragment.registerCamera(onPhoto: (Bitmap) -> Unit) =
    registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp -> bmp?.let(onPhoto) }

/** Geolocalización: pide el permiso en tiempo de ejecución y entrega la última ubicación conocida. */
class LocationHelper(private val fragment: Fragment, private val onResult: (String) -> Unit) {
    private val launcher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) read() else onResult(fragment.getString(R.string.location_denied))
    }

    fun request() {
        val ctx = fragment.requireContext()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) read()
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @SuppressLint("MissingPermission")
    private fun read() {
        val lm = fragment.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
        onResult(
            if (loc != null) "%.5f, %.5f".format(loc.latitude, loc.longitude)
            else fragment.getString(R.string.location_unavailable)
        )
    }
}

fun inflater(v: View): LayoutInflater = LayoutInflater.from(v.context)

