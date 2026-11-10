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
import java.text.NumberFormat
import java.util.Locale

private val cop = NumberFormat.getIntegerInstance(Locale("es", "CO"))

/** 185000 -> "$ 185.000" */
fun Long.cop(): String = "$ " + cop.format(this)

fun View.snack(msg: String) = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()

/** Pinta una etiqueta de estado: fondo redondeado teñido con el color indicado. */
fun TextView.badge(text: String, colorRes: Int) {
    this.text = text
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
}

fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()

/** Adaptador genérico RecyclerView + ViewBinding para no repetir un adaptador por cada lista. */
class BindingAdapter<T, VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val onBind: (VB, T) -> Unit,
) : RecyclerView.Adapter<BindingAdapter.Holder<VB>>() {

    class Holder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    var items: List<T> = emptyList()
        set(value) { field = value; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder<VB>, position: Int) = onBind(holder.binding, items[position])
    override fun getItemCount() = items.size
}
