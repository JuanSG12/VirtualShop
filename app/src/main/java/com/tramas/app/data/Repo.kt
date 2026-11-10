package com.tramas.app.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tramas.app.R

/**
 * Repositorio en memoria con datos de ejemplo. Implementa el CRUD real de usuarios, productos y
 * carrito para que la interfaz sea funcional sin backend (la conexión a Firebase/Wompi va en la siguiente fase).
 */
object Repo {
    const val IVA = 0.19
    const val SHIPPING = 12_000L

    val categories = listOf("Tejidos", "Cerámica", "Café", "Joyería", "Sombreros")

    val users = mutableListOf(
        User(1, "Ana Gómez", "ana@correo.com", "300 111 2233", Role.BUYER),
        User(2, "Taller Kai", "kai@taller.co", "311 222 3344", Role.SELLER),
        User(3, "Luis Ríos", "luis@correo.com", "312 333 4455", Role.BUYER),
        User(4, "Sofía Mora", "sofia@tramas.co", "320 444 5566", Role.ADMIN),
        User(5, "Pedro Silva", "pedro@correo.com", "315 555 6677", Role.BUYER, active = false),
        User(6, "Joyas del Sur", "joyas@sur.co", "314 666 7788", Role.SELLER),
    )

    val products = mutableListOf(
        Product(1, "Mochila wayúu", "Mochila tejida a mano por artesanas wayúu en Riohacha. Cada diseño es único y cuenta una historia del territorio.", 185_000, 12, "Tejidos", R.drawable.art_mochila, "Taller Kai", rating = 4.9, distanceKm = 3.2),
        Product(2, "Taza de cerámica", "Taza esmaltada y pintada a mano, apta para microondas. Capacidad de 300 ml.", 42_000, 3, "Cerámica", R.drawable.art_taza, "Taller Kai", rating = 4.7, distanceKm = 3.2),
        Product(3, "Café de origen 500 g", "Café especial de altura, tostión media. Notas de panela y cacao. Cultivado por familias de Nariño.", 28_500, 40, "Café", R.drawable.art_cafe, "Finca La Aurora", rating = 4.8, distanceKm = 12.0),
        Product(4, "Collar en plata", "Collar con cuentas de plata 950 y dije en latón dorado, elaborado por orfebres de Mompox.", 96_000, 8, "Joyería", R.drawable.art_collar, "Joyas del Sur", ProductStatus.PENDING, rating = 4.6, distanceKm = 5.5),
        Product(5, "Sombrero vueltiao", "Sombrero tradicional en caña flecha, 19 vueltas. Patrimonio cultural de Colombia.", 240_000, 5, "Sombreros", R.drawable.art_sombrero, "Taller Zenú", ProductStatus.PENDING, rating = 5.0, distanceKm = 8.1),
        Product(6, "Tapete tejido", "Tapete de lana con patrón geométrico, tintes naturales. Medidas 60 x 90 cm.", 130_000, 6, "Tejidos", R.drawable.art_tejido, "Taller Kai", rating = 4.5, distanceKm = 3.2),
        Product(7, "Café molido 250 g", "Café molido para filtro, tostión clara. Presentación en bolsa kraft.", 17_000, 55, "Café", R.drawable.art_cafe, "Finca La Aurora", rating = 4.4, distanceKm = 12.0),
        Product(8, "Set de 2 tazas", "Par de tazas de gres con acabado rústico.", 78_000, 9, "Cerámica", R.drawable.art_taza, "Taller Kai", ProductStatus.REJECTED, rating = 4.2, distanceKm = 3.2),
    )

    val orders = mutableListOf(
        Order(1042, "Ana Gómez", "12 oct 2026", 3, 366_025, OrderStatus.PAID),
        Order(1041, "Pedro Silva", "11 oct 2026", 1, 185_000, OrderStatus.PAID, "Medellín"),
        Order(1040, "María Díaz", "10 oct 2026", 2, 70_500, OrderStatus.PREPARING, "Cali"),
        Order(1031, "Luis Ríos", "08 oct 2026", 1, 96_000, OrderStatus.SHIPPED),
        Order(1007, "Ana Gómez", "01 oct 2026", 2, 214_000, OrderStatus.DELIVERED),
        Order(994, "Juan Pérez", "27 sep 2026", 1, 96_000, OrderStatus.REJECTED),
    )

    val salesByCategory = listOf(
        CategorySale("Tejidos", 18_400_000), CategorySale("Café", 11_200_000), CategorySale("Cerámica", 6_300_000),
        CategorySale("Joyería", 4_100_000), CategorySale("Sombreros", 2_300_000),
    )

    // ---------- Usuarios (CRUD) ----------
    private fun nextUserId() = (users.maxOfOrNull { it.id } ?: 0) + 1
    fun user(id: Int) = users.firstOrNull { it.id == id }
    fun addUser(name: String, email: String, phone: String, role: Role, active: Boolean) {
        users.add(User(nextUserId(), name, email, phone, role, active))
    }
    fun deleteUser(id: Int) { users.removeAll { it.id == id } }

    // ---------- Productos (CRUD) ----------
    private fun nextProductId() = (products.maxOfOrNull { it.id } ?: 0) + 1
    fun product(id: Int) = products.firstOrNull { it.id == id }
    fun catalog() = products.filter { it.status == ProductStatus.ACTIVE }
    fun sellerProducts(seller: String) = products.filter { it.seller == seller }

    fun saveProduct(id: Int, p: Product): Product {
        return if (id < 0) {
            p.id = nextProductId(); p.status = ProductStatus.PENDING
            products.add(p); p
        } else {
            val cur = product(id)!!
            cur.name = p.name; cur.description = p.description; cur.price = p.price
            cur.stock = p.stock; cur.category = p.category; cur.photo = p.photo ?: cur.photo
            cur
        }
    }
    fun deleteProduct(id: Int) {
        products.removeAll { it.id == id }
        removeFromCart(id)
    }

    // ---------- Carrito (CRUD) ----------
    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> = _cart
    private val items get() = _cart.value.orEmpty()

    fun addToCart(p: Product, qty: Int = 1) {
        val list = items.map { it.copy() }.toMutableList()
        val cur = list.firstOrNull { it.product.id == p.id }
        if (cur != null) cur.qty = (cur.qty + qty).coerceAtMost(p.stock) else list.add(CartItem(p, qty.coerceAtMost(p.stock)))
        _cart.value = list
    }
    fun setQty(productId: Int, qty: Int) {
        _cart.value = items.map { if (it.product.id == productId) it.copy(qty = qty.coerceIn(1, it.product.stock)) else it.copy() }
    }
    fun removeFromCart(productId: Int) { _cart.value = items.filter { it.product.id != productId } }
    fun clearCart() { _cart.value = emptyList() }

    fun subtotal() = items.sumOf { it.product.price * it.qty }
    fun iva() = (subtotal() * IVA).toLong()
    fun shipping() = if (items.isEmpty()) 0L else SHIPPING
    fun total() = subtotal() + iva() + shipping()
    fun cartCount() = items.sumOf { it.qty }

    fun placeOrder(): Order {
        val o = Order((orders.maxOfOrNull { it.id } ?: 1000) + 1, Session.name, "13 nov 2026", cartCount(), total(), OrderStatus.PAID)
        orders.add(0, o)
        items.forEach { it.product.stock = (it.product.stock - it.qty).coerceAtLeast(0) }
        clearCart()
        return o
    }
}
