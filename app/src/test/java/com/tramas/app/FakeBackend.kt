package com.tramas.app

import com.tramas.app.data.AppException
import com.tramas.app.data.AuthRepository
import com.tramas.app.data.CartItem
import com.tramas.app.data.CartRepository
import com.tramas.app.data.ImageStorage
import com.tramas.app.data.Order
import com.tramas.app.data.OrderItem
import com.tramas.app.data.OrderRepository
import com.tramas.app.data.OrderStatus
import com.tramas.app.data.Product
import com.tramas.app.data.ProductRepository
import com.tramas.app.data.ProductStatus
import com.tramas.app.data.Role
import com.tramas.app.data.Services
import com.tramas.app.data.User
import com.tramas.app.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Backend en memoria para pruebas y capturas: reemplaza a Firebase con los mismos contratos (interfaces). */
object FakeBackend {
    val admin = User("u-admin", "Sofía Mora", "sofia@tramas.co", "320 444 5566", Role.ADMIN)
    val seller = User("u-seller", "Taller Kai", "kai@taller.co", "311 222 3344", Role.SELLER, nit = "900.123.456-7", craft = "Tejeduría wayúu", address = "Riohacha, La Guajira")
    val buyer = User("u-buyer", "Juan Pérez", "juan@correo.com", "300 111 2233", Role.BUYER, address = "Cra 7 # 45-12, Bogotá")

    val users = MutableStateFlow(listOf(admin, seller, buyer,
        User("u2", "Ana Gómez", "ana@correo.com", "300 111 2233", Role.BUYER),
        User("u3", "Pedro Silva", "pedro@correo.com", "315 555 6677", Role.BUYER, active = false)))

    val products = MutableStateFlow(listOf(
        Product("p1", "Mochila wayúu", "Mochila tejida a mano por artesanas wayúu en Riohacha.", 185_000, 12, "Tejidos", seller.id, seller.name, ProductStatus.ACTIVE, 11.54, -72.9),
        Product("p2", "Taza de cerámica", "Taza esmaltada y pintada a mano.", 42_000, 3, "Cerámica", seller.id, seller.name, ProductStatus.ACTIVE),
        Product("p3", "Café de origen 500 g", "Café especial de altura, tostión media.", 28_500, 40, "Café", "u-other", "Finca La Aurora", ProductStatus.ACTIVE),
        Product("p4", "Collar en plata", "Collar con cuentas de plata 950.", 96_000, 8, "Joyería", seller.id, seller.name, ProductStatus.PENDING),
        Product("p5", "Sombrero vueltiao", "Sombrero tradicional en caña flecha.", 240_000, 5, "Sombreros", "u-other", "Taller Zenú", ProductStatus.PENDING)))

    val orders = MutableStateFlow(listOf(
        Order("o1", buyer.id, buyer.name, buyer.email, listOf(OrderItem("p1", "Mochila wayúu", 185_000, 1, "Tejidos", seller.id)), 185_000, 35_150, 12_000, 232_150, OrderStatus.PAID, "Cra 7 # 45-12, Bogotá"),
        Order("o2", buyer.id, buyer.name, buyer.email, listOf(OrderItem("p2", "Taza de cerámica", 42_000, 2, "Cerámica", seller.id)), 84_000, 15_960, 12_000, 111_960, OrderStatus.SHIPPED, "Cra 7 # 45-12, Bogotá")))

    val cart = MutableStateFlow(listOf(
        CartItem("p1", "Mochila wayúu", 185_000, 1, 12, "Tejidos", seller.id, seller.name, null),
        CartItem("p2", "Taza de cerámica", 42_000, 2, 3, "Cerámica", seller.id, seller.name, null)))

    /** Deja los servicios apuntando a este backend en memoria. */
    fun install() {
        Services.auth = object : AuthRepository {
            override val currentUid: String? = null
            override suspend fun signIn(email: String, password: String) = users.value.firstOrNull { it.email == email } ?: throw AppException("Correo o contraseña incorrectos")
            override suspend fun register(name: String, email: String, phone: String, password: String, role: Role) =
                User("u-${users.value.size}", name, email, phone, role).also { users.value = users.value + it }
            override suspend fun loadCurrentProfile(): User? = null
            override fun signOut() {}
            override suspend fun sendPasswordReset(email: String) {}
            override suspend fun deleteOwnAccount() {}
        }
        Services.users = object : UserRepository {
            override fun observeAll(): Flow<List<User>> = users
            override suspend fun get(uid: String) = users.value.firstOrNull { it.id == uid }
            override suspend fun save(user: User) { users.value = users.value.map { if (it.id == user.id) user else it } }
            override suspend fun createWithAuth(name: String, email: String, phone: String, password: String, role: Role, active: Boolean) =
                User("u-${users.value.size + 1}", name, email, phone, role, active).also { users.value = users.value + it }
            override suspend fun delete(uid: String) { users.value = users.value.filter { it.id != uid } }
        }
        Services.products = object : ProductRepository {
            override fun observeCatalog() = products.map { l -> l.filter { it.status == ProductStatus.ACTIVE } }
            override fun observeBySeller(sellerId: String) = products.map { l -> l.filter { it.sellerId == sellerId } }
            override fun observeAll(): Flow<List<Product>> = products
            override suspend fun get(id: String) = products.value.firstOrNull { it.id == id }
            override suspend fun save(product: Product, image: ByteArray?): Product {
                val saved = if (product.id.isEmpty()) product.copy(id = "p${products.value.size + 1}") else product
                products.value = products.value.filter { it.id != saved.id } + saved
                return saved
            }
            override suspend fun delete(id: String) { products.value = products.value.filter { it.id != id } }
            override suspend fun setStatus(id: String, status: ProductStatus) { products.value = products.value.map { if (it.id == id) it.copy(status = status) else it } }
            override suspend fun decrementStock(items: List<OrderItem>) {
                products.value = products.value.map { p -> items.firstOrNull { it.productId == p.id }?.let { p.copy(stock = p.stock - it.qty) } ?: p }
            }
        }
        Services.cart = object : CartRepository {
            override fun observe(uid: String): Flow<List<CartItem>> = cart
            override suspend fun add(uid: String, product: Product, qty: Int) {
                val cur = cart.value.firstOrNull { it.productId == product.id }
                cart.value = if (cur != null) cart.value.map { if (it === cur) it.copy(qty = (it.qty + qty).coerceAtMost(product.stock)) else it }
                else cart.value + CartItem(product.id, product.name, product.price, qty, product.stock, product.category, product.sellerId, product.sellerName, product.imageUrl)
            }
            override suspend fun setQty(uid: String, productId: String, qty: Int) { cart.value = cart.value.map { if (it.productId == productId) it.copy(qty = qty) else it } }
            override suspend fun remove(uid: String, productId: String) { cart.value = cart.value.filter { it.productId != productId } }
            override suspend fun clear(uid: String) { cart.value = emptyList() }
        }
        Services.orders = object : OrderRepository {
            override suspend fun create(order: Order): String { val id = "o${orders.value.size + 1}"; orders.value = orders.value + order.copy(id = id); return id }
            override fun observeForBuyer(uid: String) = orders.map { l -> l.filter { it.buyerId == uid } }
            override fun observeForSeller(uid: String) = orders.map { l -> l.filter { uid in it.sellerIds } }
            override fun observeAll(): Flow<List<Order>> = orders
            override suspend fun updateStatus(id: String, status: OrderStatus) { orders.value = orders.value.map { if (it.id == id) it.copy(status = status) else it } }
            override suspend fun setPayment(id: String, ref: String, state: String, mode: String, status: OrderStatus) {
                orders.value = orders.value.map { if (it.id == id) it.copy(paymentRef = ref, paymentState = state, paymentMode = mode, status = status) else it }
            }
        }
        Services.images = object : ImageStorage { override suspend fun upload(path: String, bytes: ByteArray) = "data:image/jpeg;base64," }
        Services.ready = true
    }
}

/** Application de pruebas: usa el backend en memoria en vez de Firebase. */
class TestApp : TramasApp() {
    override fun installServices() = FakeBackend.install()
}
