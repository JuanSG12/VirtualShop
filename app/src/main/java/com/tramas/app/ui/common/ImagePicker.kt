package com.tramas.app.ui.common

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Selector de imagen: pregunta si se quiere usar la CÁMARA o la GALERÍA y entrega la imagen ya reducida y comprimida (JPEG).
 * Debe crearse al construir el Fragment (los launchers se registran antes de que inicie la pantalla).
 */
class ImagePicker(private val fragment: Fragment, private val onImage: (ByteArray) -> Unit) {

    /** Uri del archivo temporal donde la cámara guarda la foto en calidad completa. */
    private var pending: Uri? = null

    private val camera = fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) pending?.let { process(it) }
    }
    private val gallery = fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { process(it) }
    }

    /** Muestra el diálogo "Tomar foto / Elegir de la galería". */
    fun show() {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.choose_photo_source)
            .setItems(arrayOf(fragment.getString(R.string.take_photo), fragment.getString(R.string.from_gallery))) { _, which ->
                if (which == 0) openCamera() else gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            .show()
    }

    /** Crea un archivo temporal en la caché y abre la cámara para que escriba la foto ahí (vía FileProvider). */
    private fun openCamera() {
        val ctx = fragment.requireContext()
        val dir = File(ctx.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        pending = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        camera.launch(pending!!)
    }

    /** Reduce y comprime la imagen fuera del hilo principal y la entrega al llamador. */
    private fun process(uri: Uri) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val bytes = withContext(Dispatchers.IO) { ImageUtils.compress(fragment.requireContext(), uri) }
            onImage(bytes)
        }
    }
}
