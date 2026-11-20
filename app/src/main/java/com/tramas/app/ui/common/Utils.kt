package com.tramas.app.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.tramas.app.data.AppException
import java.text.NumberFormat
import java.util.Locale

private val cop = NumberFormat.getIntegerInstance(Locale("es", "CO"))

/** Formatea pesos colombianos: 185000 -> "$ 185.000". */
fun Long.cop(): String = "$ " + cop.format(this)

/** Muestra un mensaje breve en la parte inferior de la pantalla. */
fun View.snack(msg: String) = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()

/** Pinta una etiqueta de estado: fondo redondeado teñido con el color indicado. */
fun TextView.badge(text: String, colorRes: Int) {
    this.text = text
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
}

/** Convierte dp a píxeles. */
fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()

/** Mensaje en español para un error de Firebase / red / negocio (en vez de mostrar el texto técnico). */
fun Throwable.friendly(): String {
    if (this is AppException) return message ?: "Ocurrió un error"
    val code = (this as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
    return when {
        code == "ERROR_INVALID_CREDENTIAL" || code == "ERROR_WRONG_PASSWORD" || code == "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos"
        code == "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya está registrado"
        code == "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil (mínimo 6 caracteres)"
        code == "ERROR_INVALID_EMAIL" -> "El correo no es válido"
        code == "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada"
        code == "ERROR_REQUIRES_RECENT_LOGIN" -> "Por seguridad, cierra sesión y vuelve a ingresar para hacer esto"
        this is com.google.firebase.FirebaseNetworkException || this is java.net.UnknownHostException -> "Sin conexión a internet"
        this is com.google.firebase.firestore.FirebaseFirestoreException && this.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "No tienes permiso para esta acción (revisa las reglas de Firestore)"
        else -> message ?: "Ocurrió un error inesperado"
    }
}

/** Adaptador genérico RecyclerView + ViewBinding para no repetir un adaptador por cada lista. */
class BindingAdapter<T, VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val onBind: (VB, T) -> Unit,
) : RecyclerView.Adapter<BindingAdapter.Holder<VB>>() {

    /** Contenedor de la vista de una fila (guarda su binding). */
    class Holder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    var items: List<T> = emptyList()
        set(value) { field = value; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder<VB>, position: Int) = onBind(holder.binding, items[position])
    override fun getItemCount() = items.size
}
