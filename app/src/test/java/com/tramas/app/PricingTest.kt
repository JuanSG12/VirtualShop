package com.tramas.app

import com.tramas.app.data.CartItem
import com.tramas.app.data.Pricing
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas de las reglas de precio del carrito (IVA 19 % + envío fijo). */
class PricingTest {
    private fun item(price: Long, qty: Int) = CartItem("p", "x", price, qty, 99, "Café", "s", "S", null)

    @Test fun totalIncluyeIvaYEnvio() {
        val items = listOf(item(100_000, 2), item(50_000, 1)) // subtotal 250.000
        assertEquals(250_000, Pricing.subtotal(items))
        assertEquals(47_500, Pricing.iva(items))
        assertEquals(12_000, Pricing.shipping(items))
        assertEquals(309_500, Pricing.total(items))
        assertEquals(3, Pricing.count(items))
    }

    @Test fun carritoVacioNoCobraEnvio() {
        assertEquals(0, Pricing.total(emptyList()))
    }
}
