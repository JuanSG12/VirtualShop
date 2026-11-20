package com.tramas.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/** Utilidades para preparar y mostrar imágenes (reducción de tamaño y data-URI). */
object ImageUtils {

    /**
     * Lee una imagen (cámara o galería), la reduce a [maxPx] en su lado mayor y la comprime a JPEG.
     * Así la subida a Storage es rápida y no se gasta cuota en fotos de varios MB.
     */
    fun compress(context: Context, uri: Uri, maxPx: Int = 1024, quality: Int = 80): ByteArray {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("No se pudo leer la imagen")
        return shrink(bytes, maxPx, quality)
    }

    /** Reduce una imagen ya cargada en memoria (se usa también como plan B cuando Storage no está disponible). */
    fun shrink(bytes: ByteArray, maxPx: Int, quality: Int): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxPx || bounds.outHeight / (sample * 2) >= maxPx) sample *= 2
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("Imagen inválida")
        val scale = maxPx.toFloat() / maxOf(bmp.width, bmp.height)
        val out = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
        return ByteArrayOutputStream().also { out.compress(Bitmap.CompressFormat.JPEG, quality, it) }.toByteArray()
    }

    /** Convierte bytes JPEG (reducidos a 640 px) en un data-URI que cabe en un documento de Firestore (límite 1 MB). */
    fun toDataUri(bytes: ByteArray): String =
        "data:image/jpeg;base64," + Base64.encodeToString(shrink(bytes, 640, 70), Base64.NO_WRAP)

    /** Decodifica un data-URI a Bitmap (null si no es un data-URI válido). */
    fun decodeDataUri(uri: String): Bitmap? = runCatching {
        val raw = Base64.decode(uri.substringAfter("base64,"), Base64.DEFAULT)
        BitmapFactory.decodeByteArray(raw, 0, raw.size)
    }.getOrNull()
}
