package com.tramas.app.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.tramas.app.data.CartItem
import com.tramas.app.data.Order
import com.tramas.app.data.OrderItem
import com.tramas.app.data.OrderStatus
import com.tramas.app.data.Product
import com.tramas.app.data.ProductStatus
import com.tramas.app.data.Role
import com.tramas.app.data.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * Conversión manual entre documentos de Firestore (Map) y los modelos de la app.
 * Se hace a mano (en vez de toObject) para controlar los nombres de campo y los valores por defecto.
 */

/** Convierte un valor de Firestore a Long (0 si falta). */
private fun Any?.long(): Long = (this as? Number)?.toLong() ?: 0L
/** Convierte un valor de Firestore a Int (0 si falta). */
private fun Any?.int(): Int = (this as? Number)?.toInt() ?: 0
/** Convierte un valor de Firestore a Double (null si falta). */
private fun Any?.dbl(): Double? = (this as? Number)?.toDouble()

/** Escucha en tiempo real una consulta: emite la lista completa cada vez que cambia algún documento. */
internal fun <T> Query.observe(map: (DocumentSnapshot) -> T?): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snap, error ->
        if (error != null) { close(error); return@addSnapshotListener }
        trySend(snap?.documents?.mapNotNull(map).orEmpty())
    }
    awaitClose { registration.remove() }
}

// ---------------------------------------------------------------- Usuario
fun User.toMap(): Map<String, Any?> = mapOf(
    "name" to name, "email" to email, "phone" to phone, "role" to role.name, "active" to active,
    "address" to address, "lat" to lat, "lng" to lng, "photoUrl" to photoUrl,
    "nit" to nit, "craft" to craft, "radiusKm" to radiusKm,
)

/** Documento de Firestore -> User. */
fun DocumentSnapshot.toUser(): User? {
    val d = data ?: return null
    return User(
        id = id, name = d["name"] as? String ?: "", email = d["email"] as? String ?: "", phone = d["phone"] as? String ?: "",
        role = Role.from(d["role"] as? String), active = d["active"] as? Boolean ?: true,
        address = d["address"] as? String ?: "", lat = d["lat"].dbl(), lng = d["lng"].dbl(), photoUrl = d["photoUrl"] as? String,
        nit = d["nit"] as? String ?: "", craft = d["craft"] as? String ?: "", radiusKm = d["radiusKm"]?.int() ?: 25,
    )
}

// ---------------------------------------------------------------- Producto
fun Product.toMap(): Map<String, Any?> = mapOf(
    "name" to name, "description" to description, "price" to price, "stock" to stock, "category" to category,
    "sellerId" to sellerId, "sellerName" to sellerName, "status" to status.name,
    "lat" to lat, "lng" to lng, "imageUrl" to imageUrl, "createdAt" to createdAt,
)

/** Documento de Firestore -> Product. */
fun DocumentSnapshot.toProduct(): Product? {
    val d = data ?: return null
    return Product(
        id = id, name = d["name"] as? String ?: "", description = d["description"] as? String ?: "",
        price = d["price"].long(), stock = d["stock"].int(), category = d["category"] as? String ?: "",
        sellerId = d["sellerId"] as? String ?: "", sellerName = d["sellerName"] as? String ?: "",
        status = ProductStatus.values().firstOrNull { it.name == d["status"] } ?: ProductStatus.PENDING,
        lat = d["lat"].dbl(), lng = d["lng"].dbl(), imageUrl = d["imageUrl"] as? String, createdAt = d["createdAt"].long(),
    )
}

// ---------------------------------------------------------------- Carrito
fun CartItem.toMap(): Map<String, Any?> = mapOf(
    "productId" to productId, "name" to name, "price" to price, "qty" to qty, "stock" to stock,
    "category" to category, "sellerId" to sellerId, "sellerName" to sellerName, "imageUrl" to imageUrl,
)

/** Documento de Firestore -> CartItem. */
fun DocumentSnapshot.toCartItem(): CartItem? {
    val d = data ?: return null
    return CartItem(
        productId = id, name = d["name"] as? String ?: "", price = d["price"].long(), qty = d["qty"].int(), stock = d["stock"].int(),
        category = d["category"] as? String ?: "", sellerId = d["sellerId"] as? String ?: "",
        sellerName = d["sellerName"] as? String ?: "", imageUrl = d["imageUrl"] as? String,
    )
}

// ---------------------------------------------------------------- Orden
fun Order.toMap(): Map<String, Any?> = mapOf(
    "buyerId" to buyerId, "buyerName" to buyerName, "buyerEmail" to buyerEmail,
    "items" to items.map { mapOf("productId" to it.productId, "name" to it.name, "price" to it.price, "qty" to it.qty, "category" to it.category, "sellerId" to it.sellerId) },
    "sellerIds" to sellerIds,
    "subtotal" to subtotal, "iva" to iva, "shipping" to shipping, "total" to total, "status" to status.name,
    "address" to address, "lat" to lat, "lng" to lng,
    "paymentRef" to paymentRef, "paymentState" to paymentState, "paymentMode" to paymentMode, "createdAt" to createdAt,
)

/** Documento de Firestore -> Order (con sus líneas). */
@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toOrder(): Order? {
    val d = data ?: return null
    val items = (d["items"] as? List<Map<String, Any?>>).orEmpty().map {
        OrderItem(it["productId"] as? String ?: "", it["name"] as? String ?: "", it["price"].long(), it["qty"].int(),
            it["category"] as? String ?: "", it["sellerId"] as? String ?: "")
    }
    return Order(
        id = id, buyerId = d["buyerId"] as? String ?: "", buyerName = d["buyerName"] as? String ?: "", buyerEmail = d["buyerEmail"] as? String ?: "",
        items = items, subtotal = d["subtotal"].long(), iva = d["iva"].long(), shipping = d["shipping"].long(), total = d["total"].long(),
        status = OrderStatus.values().firstOrNull { it.name == d["status"] } ?: OrderStatus.PENDING_PAYMENT,
        address = d["address"] as? String ?: "", lat = d["lat"].dbl(), lng = d["lng"].dbl(),
        paymentRef = d["paymentRef"] as? String ?: "", paymentState = d["paymentState"] as? String ?: "",
        paymentMode = d["paymentMode"] as? String ?: "", createdAt = d["createdAt"].long(),
    )
}
