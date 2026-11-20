package com.tramas.app.data

import kotlinx.coroutines.flow.Flow

/** Autenticación (Firebase Authentication) + lectura del perfil asociado. */
interface AuthRepository {
    /** UID del usuario con sesión persistente en el dispositivo, o null. */
    val currentUid: String?

    /** Inicia sesión y devuelve el perfil. Lanza [AppException] si la cuenta no existe en Firestore o está suspendida. */
    suspend fun signIn(email: String, password: String): User

    /** Crea la cuenta (solo roles COMPRADOR o VENDEDOR) y su perfil en Firestore. */
    suspend fun register(name: String, email: String, phone: String, password: String, role: Role): User

    /** Perfil del usuario con sesión persistente (null si no hay sesión o su perfil fue eliminado). */
    suspend fun loadCurrentProfile(): User?

    /** Cierra la sesión de Firebase en este dispositivo. */
    fun signOut()

    /** Envía el correo de restablecimiento de contraseña. */
    suspend fun sendPasswordReset(email: String)

    /** Elimina el perfil y la cuenta de autenticación del usuario actual. */
    suspend fun deleteOwnAccount()
}

/** CRUD de usuarios en la colección `users` de Firestore (lo usa el administrador y cada usuario sobre su perfil). */
interface UserRepository {
    /** Lista en tiempo real de todos los usuarios (solo permitido al administrador). */
    fun observeAll(): Flow<List<User>>
    /** Lee un documento por id (null si no existe). */
    suspend fun get(uid: String): User?

    /** Actualiza los datos de un perfil existente. */
    suspend fun save(user: User)

    /** Crea una cuenta completa (Authentication + perfil) sin cerrar la sesión del administrador. */
    suspend fun createWithAuth(name: String, email: String, phone: String, password: String, role: Role, active: Boolean): User

    /** Elimina el perfil; la cuenta queda bloqueada porque la app exige que el perfil exista para iniciar sesión. */
    suspend fun delete(uid: String)
}

/** CRUD de productos en la colección `products`. */
interface ProductRepository {
    /** Productos aprobados (visibles para compradores). */
    fun observeCatalog(): Flow<List<Product>>
    /** Productos de un vendedor, en tiempo real. */
    fun observeBySeller(sellerId: String): Flow<List<Product>>
    /** Todos los productos, sin importar el estado (moderación del administrador). */
    fun observeAll(): Flow<List<Product>>
    /** Lee un documento por id (null si no existe). */
    suspend fun get(id: String): Product?

    /** Crea (id vacío) o actualiza un producto; si [image] no es null la sube a Storage y guarda su URL. */
    suspend fun save(product: Product, image: ByteArray?): Product
    /** Elimina el documento. */
    suspend fun delete(id: String)
    /** Cambia el estado de moderación del producto. */
    suspend fun setStatus(id: String, status: ProductStatus)

    /** Descuenta del inventario las unidades compradas. */
    suspend fun decrementStock(items: List<OrderItem>)
}

/** Carrito de compras de cada usuario (subcolección `users/{uid}/cart`). */
interface CartRepository {
    /** Líneas del carrito del usuario, en tiempo real. */
    fun observe(uid: String): Flow<List<CartItem>>
    /** Agrega un producto al carrito (suma la cantidad si ya estaba, sin pasar del stock). */
    suspend fun add(uid: String, product: Product, qty: Int)
    /** Cambia la cantidad de una línea (entre 1 y el stock). */
    suspend fun setQty(uid: String, productId: String, qty: Int)
    /** Quita una línea del carrito. */
    suspend fun remove(uid: String, productId: String)
    /** Vacía el carrito. */
    suspend fun clear(uid: String)
}

/** Órdenes de compra (colección `orders`): historial del comprador, pedidos del vendedor y reportes del administrador. */
interface OrderRepository {
    /** Crea la orden y devuelve su id. */
    suspend fun create(order: Order): String
    /** Órdenes de un comprador, en tiempo real. */
    fun observeForBuyer(uid: String): Flow<List<Order>>
    /** Órdenes que incluyen productos de un vendedor, en tiempo real. */
    fun observeForSeller(uid: String): Flow<List<Order>>
    /** Todas las órdenes, en tiempo real (reportes del administrador). */
    fun observeAll(): Flow<List<Order>>
    /** Cambia el estado de la orden (preparando, enviado, entregado...). */
    suspend fun updateStatus(id: String, status: OrderStatus)

    /** Registra el resultado del pago (referencia, estado de la pasarela y nuevo estado de la orden). */
    suspend fun setPayment(id: String, ref: String, state: String, mode: String, status: OrderStatus)
}

/** Almacenamiento de imágenes (Firebase Storage). Devuelve la URL pública de lectura. */
interface ImageStorage {
    /** Sube los bytes de una imagen a la ruta indicada y devuelve su URL. */
    suspend fun upload(path: String, bytes: ByteArray): String
}

/**
 * Localizador de servicios: punto único donde las pantallas obtienen sus repositorios.
 * En la app real se llena con las implementaciones de Firebase ([FirebaseServices]); en las pruebas se reemplaza por simulaciones.
 */
object Services {
    lateinit var auth: AuthRepository
    lateinit var users: UserRepository
    lateinit var products: ProductRepository
    lateinit var cart: CartRepository
    lateinit var orders: OrderRepository
    lateinit var images: ImageStorage

    /** False si Firebase no pudo inicializarse (falta google-services.json); la pantalla de login lo avisa. */
    var ready: Boolean = false
}
