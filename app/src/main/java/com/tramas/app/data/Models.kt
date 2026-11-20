package com.tramas.app.data

import androidx.annotation.ColorRes
import com.tramas.app.R

/** Rol de un usuario. Determina qué interfaz se abre al iniciar sesión y qué permite hacer el servidor (reglas de Firestore). */
enum class Role(val label: String) {
    BUYER("Comprador"), SELLER("Vendedor"), ADMIN("Administrador");

    companion object {
        /** Convierte el texto guardado en Firestore al enum; si es desconocido se asume COMPRADOR (el rol de menor privilegio). */
        fun from(name: String?): Role = values().firstOrNull { it.name == name } ?: BUYER
    }
}

/** Coordenadas geográficas con la dirección textual obtenida por geocodificación inversa (puede estar vacía). */
data class GeoPoint(val lat: Double, val lng: Double, val address: String = "")

/**
 * Perfil de usuario guardado en `users/{uid}` de Firestore.
 * @property id UID de Firebase Authentication (es también el id del documento).
 * @property active Si es false la cuenta está suspendida y no puede iniciar sesión.
 */
data class User(
    val id: String,
    var name: String,
    var email: String,
    var phone: String = "",
    var role: Role = Role.BUYER,
    var active: Boolean = true,
    var address: String = "",
    var lat: Double? = null,
    var lng: Double? = null,
    var photoUrl: String? = null,
    /** Datos propios del vendedor (perfil de vendedor). */
    var nit: String = "",
    var craft: String = "",
    var radiusKm: Int = 25,
)

/** Estado de moderación de un producto: solo los ACTIVE aparecen en el catálogo del comprador. */
enum class ProductStatus(val label: String, @ColorRes val color: Int) {
    ACTIVE("Activo", R.color.success),
    PENDING("En revisión", R.color.warning),
    REJECTED("Rechazado", R.color.danger),
}

/**
 * Producto publicado por un vendedor (`products/{id}`).
 * @property imageUrl URL de Firebase Storage (o un data-URI base64 si Storage no está disponible); null = sin foto.
 */
data class Product(
    val id: String = "",
    var name: String,
    var description: String,
    var price: Long,
    var stock: Int,
    var category: String,
    val sellerId: String,
    var sellerName: String,
    var status: ProductStatus = ProductStatus.PENDING,
    var lat: Double? = null,
    var lng: Double? = null,
    var imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Línea del carrito (`users/{uid}/cart/{productId}`); guarda una copia de los datos del producto para mostrarlo sin consultas extra. */
data class CartItem(
    val productId: String,
    val name: String,
    val price: Long,
    var qty: Int,
    val stock: Int,
    val category: String,
    val sellerId: String,
    val sellerName: String,
    val imageUrl: String?,
)

/** Línea de una orden: copia inmutable del producto en el momento de la compra. */
data class OrderItem(
    val productId: String,
    val name: String,
    val price: Long,
    val qty: Int,
    val category: String,
    val sellerId: String,
)

/** Ciclo de vida de una orden, desde que se crea hasta la entrega. */
enum class OrderStatus(val label: String, @ColorRes val color: Int) {
    PENDING_PAYMENT("Pendiente de pago", R.color.warning),
    PAID("Pagado", R.color.success),
    PREPARING("Preparando", R.color.warning),
    SHIPPED("Enviado", R.color.indigo),
    DELIVERED("Entregado", R.color.teal),
    REJECTED("Pago rechazado", R.color.danger),
    CANCELLED("Cancelado", R.color.neutral);

    /** Una orden cuenta como venta cuando el pago fue aprobado y no se canceló. */
    val isSale: Boolean get() = this == PAID || this == PREPARING || this == SHIPPED || this == DELIVERED
}

/**
 * Orden de compra (`orders/{id}`): es el historial de compras del comprador y los pedidos recibidos del vendedor.
 * @property sellerIds Ids de los vendedores involucrados; permite a cada vendedor consultar solo sus pedidos.
 * @property paymentMode "EPAYCO" (transacción real en pruebas/producción) o "SIMULADO" (sin llaves configuradas).
 */
data class Order(
    val id: String = "",
    val buyerId: String,
    val buyerName: String,
    val buyerEmail: String,
    val items: List<OrderItem>,
    val subtotal: Long,
    val iva: Long,
    val shipping: Long,
    val total: Long,
    var status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val address: String,
    val lat: Double? = null,
    val lng: Double? = null,
    var paymentRef: String = "",
    var paymentState: String = "",
    var paymentMode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    val sellerIds: List<String> get() = items.map { it.sellerId }.distinct()
    val itemsCount: Int get() = items.sumOf { it.qty }
}

/** Total de ventas de una categoría (para el reporte del administrador). */
data class CategorySale(val category: String, val amount: Long)

/** Error de negocio con un mensaje listo para mostrar al usuario (ej. "Cuenta suspendida"). */
class AppException(message: String) : Exception(message)
