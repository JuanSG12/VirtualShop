package com.tramas.app.data

/** Reglas de precio del carrito: IVA 19 % sobre el subtotal más un envío fijo cuando hay productos. */
object Pricing {
    const val IVA_RATE = 0.19
    const val SHIPPING = 12_000L

    /** Suma precio por cantidad de todas las líneas. */
    fun subtotal(items: List<CartItem>) = items.sumOf { it.price * it.qty }
    /** IVA del 19 % sobre el subtotal. */
    fun iva(items: List<CartItem>) = (subtotal(items) * IVA_RATE).toLong()
    /** Envío fijo (0 si el carrito está vacío). */
    fun shipping(items: List<CartItem>) = if (items.isEmpty()) 0L else SHIPPING
    /** Subtotal + IVA + envío. */
    fun total(items: List<CartItem>) = subtotal(items) + iva(items) + shipping(items)
    /** Cantidad total de unidades en el carrito. */
    fun count(items: List<CartItem>) = items.sumOf { it.qty }
}

/** Categorías de producto disponibles en la tienda. */
object Categories {
    val all = listOf("Tejidos", "Cerámica", "Café", "Joyería", "Sombreros")
}
