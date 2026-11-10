package com.tramas.app.data

import android.graphics.Bitmap
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.tramas.app.R

enum class Role(val label: String) { BUYER("Comprador"), SELLER("Vendedor"), ADMIN("Administrador") }

data class User(
    var id: Int,
    var name: String,
    var email: String,
    var phone: String,
    var role: Role,
    var active: Boolean = true,
)

enum class ProductStatus(val label: String, @ColorRes val color: Int) {
    ACTIVE("Activo", R.color.success),
    PENDING("En revisión", R.color.warning),
    REJECTED("Rechazado", R.color.danger),
}

data class Product(
    var id: Int,
    var name: String,
    var description: String,
    var price: Long,
    var stock: Int,
    var category: String,
    @DrawableRes var art: Int,
    var seller: String,
    var status: ProductStatus = ProductStatus.ACTIVE,
    var rating: Double = 4.8,
    var distanceKm: Double = 2.4,
    var photo: Bitmap? = null,
)

data class CartItem(val product: Product, var qty: Int)

enum class OrderStatus(val label: String, @ColorRes val color: Int) {
    PAID("Pagado", R.color.success),
    PREPARING("Preparando", R.color.warning),
    SHIPPED("Enviado", R.color.indigo),
    DELIVERED("Entregado", R.color.teal),
    REJECTED("Rechazado", R.color.danger),
}

data class Order(
    val id: Int,
    val customer: String,
    val date: String,
    val itemsCount: Int,
    val total: Long,
    var status: OrderStatus,
    val city: String = "Bogotá",
)

data class CategorySale(val category: String, val amount: Long)
