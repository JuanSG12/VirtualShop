package com.tramas.app.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Escucha un Flow (datos en tiempo real de Firestore) mientras la pantalla está visible y entrega cada valor a [block].
 * Se detiene solo al salir de la pantalla; los errores (permisos, red) se muestran en un mensaje en vez de cerrar la app.
 */
fun <T> Fragment.collectFlow(flow: Flow<T>, block: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.catch { e -> view?.snack(e.friendly()) }.collect { block(it) }
        }
    }
}

/**
 * Ejecuta una operación suspendida (guardar, borrar, subir) ligada al ciclo de vida de la pantalla.
 * Si falla muestra el error amigable; [onDone] se llama solo si terminó bien.
 */
fun Fragment.launchSafe(loading: View? = null, onDone: () -> Unit = {}, block: suspend () -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        loading?.isEnabled = false
        try {
            block()
            onDone()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            view?.snack(e.friendly())
        } finally {
            loading?.isEnabled = true
        }
    }
}
