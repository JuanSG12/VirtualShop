package com.tramas.app.ui.common

import android.graphics.Bitmap
import android.util.LruCache
import android.widget.ImageView
import coil.dispose
import coil.load
import com.tramas.app.R
import com.tramas.app.data.ImageUtils

/** Ilustración por categoría: se usa cuando un producto aún no tiene foto. */
fun artFor(category: String): Int = when (category) {
    "Tejidos" -> R.drawable.art_mochila
    "Cerámica" -> R.drawable.art_taza
    "Café" -> R.drawable.art_cafe
    "Joyería" -> R.drawable.art_collar
    "Sombreros" -> R.drawable.art_sombrero
    else -> R.drawable.art_tejido
}

/** Caché de imágenes data-URI ya decodificadas (evita decodificar el mismo base64 en cada fila de la lista). */
private val dataUriCache = LruCache<String, Bitmap>(24)

/**
 * Muestra la foto de un producto:
 *  - URL de Firebase Storage -> se descarga con Coil (con la ilustración de la categoría como marcador);
 *  - data-URI (plan B sin Storage) -> se decodifica;
 *  - sin foto -> ilustración de la categoría.
 */
fun ImageView.loadProduct(url: String?, category: String) {
    val fallback = artFor(category)
    // dispose() cancela una descarga anterior de esta vista (las filas del RecyclerView se reciclan).
    dispose()
    when {
        url.isNullOrBlank() -> setImageResource(fallback)
        url.startsWith("data:") -> {
            val bmp = dataUriCache.get(url) ?: ImageUtils.decodeDataUri(url)?.also { dataUriCache.put(url, it) }
            if (bmp != null) setImageBitmap(bmp) else setImageResource(fallback)
        }
        else -> load(url) { placeholder(fallback); error(fallback); crossfade(true) }
    }
}

/** Foto de perfil o logo: URL/data-URI; si no hay, deja el ícono por defecto que ya tiene la vista. */
fun ImageView.loadAvatar(url: String?) {
    if (url.isNullOrBlank()) return
    imageTintList = null
    setPadding(0, 0, 0, 0)
    loadProduct(url, "")
}
