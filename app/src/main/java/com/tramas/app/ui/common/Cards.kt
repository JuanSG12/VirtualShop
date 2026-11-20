package com.tramas.app.ui.common

import androidx.appcompat.app.AlertDialog
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Product
import com.tramas.app.data.Session
import com.tramas.app.databinding.ItemProductCardBinding
import com.tramas.app.databinding.ViewEmptyStateBinding
import androidx.core.view.isVisible

/** Llena la tarjeta de producto del catálogo (foto, nombre, distancia al usuario, precio) y conecta sus acciones. */
fun ItemProductCardBinding.bindProduct(p: Product, onOpen: (Product) -> Unit, onAdd: (Product) -> Unit) {
    imgArt.loadProduct(p.imageUrl, p.category)
    tvName.text = p.name
    tvRating.text = distanceKm(Session.location, p.lat, p.lng).kmText() + " · " + p.sellerName
    tvPrice.text = p.price.cop()
    btnAdd.isEnabled = p.stock > 0
    root.setOnClickListener { onOpen(p) }
    btnAdd.setOnClickListener { onAdd(p) }
}

/** Muestra u oculta el estado vacío ("sin resultados") con un título y un texto. */
fun ViewEmptyStateBinding.show(visible: Boolean, title: String, text: String) {
    root.isVisible = visible
    tvEmptyTitle.text = title
    tvEmptyText.text = text
}

/** Diálogo de confirmación estándar para acciones destructivas (eliminar, vaciar, cerrar sesión). */
fun Context.confirm(title: Int, message: String, positive: Int, onYes: () -> Unit): AlertDialog =
    MaterialAlertDialogBuilder(this)
        .setTitle(title).setMessage(message)
        .setPositiveButton(positive) { _, _ -> onYes() }
        .setNegativeButton(R.string.cancel, null)
        .show()
