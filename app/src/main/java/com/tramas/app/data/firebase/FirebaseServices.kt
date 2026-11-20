package com.tramas.app.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.tramas.app.data.AppException
import com.tramas.app.data.AuthRepository
import com.tramas.app.data.CartItem
import com.tramas.app.data.CartRepository
import com.tramas.app.data.ImageStorage
import com.tramas.app.data.ImageUtils
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/** Instala las implementaciones de Firebase en el [Services]. Se llama una vez desde la clase Application. */
object FirebaseServices {

    /** @return true si Firebase quedó listo; false si falta google-services.json (la app lo avisa en el login). */
    fun install(context: Context): Boolean {
        return try {
            FirebaseApp.getInstance() // lanza IllegalStateException si Firebase no se inicializó
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val users = FirestoreUserRepo(context.applicationContext, db, auth)
            Services.auth = FirebaseAuthRepo(auth, db)
            Services.users = users
            // Storage puede no estar activado todavía en el proyecto: en ese caso se usa el plan B (imagen dentro de Firestore).
            val storage = runCatching { FirebaseStorage.getInstance() }.getOrNull()
            Services.images = FirebaseImageStorage(storage)
            Services.products = FirestoreProductRepo(db, Services.images)
            Services.cart = FirestoreCartRepo(db)
            Services.orders = FirestoreOrderRepo(db)
            Services.ready = true
            true
        } catch (e: IllegalStateException) {
            Log.e("FirebaseServices", "Firebase no está configurado (falta app/google-services.json)", e)
            Services.ready = false
            false
        }
    }
}

// ====================================================================================== Autenticación
/** Autenticación con correo y contraseña (Firebase Authentication); el perfil y el rol viven en `users/{uid}`. */
class FirebaseAuthRepo(private val auth: FirebaseAuth, private val db: FirebaseFirestore) : AuthRepository {

    override val currentUid: String? get() = auth.currentUser?.uid

    /** Inicia sesión en Firebase Authentication y valida que el perfil exista y esté activo. */
    override suspend fun signIn(email: String, password: String): User {
        val uid = auth.signInWithEmailAndPassword(email.trim(), password).await().user?.uid ?: throw AppException("No se pudo iniciar sesión")
        val profile = db.collection("users").document(uid).get().await().toUser()
        if (profile == null) { auth.signOut(); throw AppException("Tu cuenta fue eliminada. Contacta al administrador.") }
        if (!profile.active) { auth.signOut(); throw AppException("Tu cuenta está suspendida. Contacta al administrador.") }
        return profile
    }

    /** Crea la cuenta en Authentication y su perfil en Firestore; envía correo de verificación. */
    override suspend fun register(name: String, email: String, phone: String, password: String, role: Role): User {
        require(role != Role.ADMIN) { "El rol administrador no se puede elegir al registrarse" }
        val uid = auth.createUserWithEmailAndPassword(email.trim(), password).await().user?.uid ?: throw AppException("No se pudo crear la cuenta")
        val user = User(uid, name.trim(), email.trim(), phone.trim(), role)
        db.collection("users").document(uid).set(user.toMap()).await()
        // Correo de verificación: es un extra, si falla no debe impedir el registro.
        runCatching { auth.currentUser?.sendEmailVerification()?.await() }
        return user
    }

    /** Lee el perfil del usuario con sesión persistente; cierra la sesión si fue eliminado o suspendido. */
    override suspend fun loadCurrentProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val profile = db.collection("users").document(uid).get().await().toUser()
        if (profile == null || !profile.active) { auth.signOut(); return null }
        return profile
    }

    override fun signOut() = auth.signOut()

    /** Pide a Firebase enviar el correo de restablecimiento de contraseña. */
    override suspend fun sendPasswordReset(email: String) { auth.sendPasswordResetEmail(email.trim()).await() }

    /** Borra el perfil de Firestore y la cuenta de Authentication. */
    override suspend fun deleteOwnAccount() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).delete().await()
        user.delete().await() // puede fallar si la sesión es antigua: Firebase pide volver a iniciar sesión
    }
}

// ====================================================================================== Usuarios
/** CRUD de usuarios sobre `users`. Para crear cuentas desde el administrador usa una segunda instancia de Firebase. */
class FirestoreUserRepo(private val context: Context, private val db: FirebaseFirestore, private val auth: FirebaseAuth) : UserRepository {

    private val col get() = db.collection("users")

    override fun observeAll(): Flow<List<User>> = col.observe { it.toUser() }

    override suspend fun get(uid: String): User? = col.document(uid).get().await().toUser()

    /** Guarda/actualiza el documento (merge para no borrar campos desconocidos). */
    override suspend fun save(user: User) { col.document(user.id).set(user.toMap(), SetOptions.merge()).await() }

    /** Crea la cuenta con una segunda instancia de Firebase para no cerrar la sesión del administrador, y escribe el perfil. */
    override suspend fun createWithAuth(name: String, email: String, phone: String, password: String, role: Role, active: Boolean): User {
        // createUserWithEmailAndPassword inicia sesión con la cuenta nueva. Para no sacar al administrador de su sesión
        // se usa una segunda app de Firebase ("secondary") solo para crear la cuenta y se cierra enseguida.
        val primary = FirebaseApp.getInstance()
        val secondary = runCatching { FirebaseApp.getInstance("secondary") }.getOrElse { FirebaseApp.initializeApp(context, primary.options, "secondary") }
        val secondaryAuth = FirebaseAuth.getInstance(secondary)
        val uid = secondaryAuth.createUserWithEmailAndPassword(email.trim(), password).await().user?.uid ?: throw AppException("No se pudo crear la cuenta")
        secondaryAuth.signOut()
        val user = User(uid, name.trim(), email.trim(), phone.trim(), role, active)
        col.document(uid).set(user.toMap()).await() // el administrador escribe el perfil con su propia sesión
        return user
    }

    /** Elimina el documento de Firestore. */
    override suspend fun delete(uid: String) { col.document(uid).delete().await() }
}

// ====================================================================================== Productos
/** CRUD de productos sobre `products` con subida de la foto a Firebase Storage. */
class FirestoreProductRepo(private val db: FirebaseFirestore, private val images: ImageStorage) : ProductRepository {

    private val col get() = db.collection("products")

    override fun observeCatalog(): Flow<List<Product>> =
        col.whereEqualTo("status", ProductStatus.ACTIVE.name).observe { it.toProduct() }

    /** Consulta productos por vendedor (un solo filtro: sin índice compuesto). */
    override fun observeBySeller(sellerId: String): Flow<List<Product>> = col.whereEqualTo("sellerId", sellerId).observe { it.toProduct() }

    override fun observeAll(): Flow<List<Product>> = col.observe { it.toProduct() }

    override suspend fun get(id: String): Product? = col.document(id).get().await().toProduct()

    /** Guarda/actualiza el documento (merge para no borrar campos desconocidos). */
    override suspend fun save(product: Product, image: ByteArray?): Product {
        val ref = if (product.id.isEmpty()) col.document() else col.document(product.id)
        val saved = product.copy(id = ref.id)
        if (image != null) saved.imageUrl = images.upload("products/${ref.id}.jpg", image)
        ref.set(saved.toMap()).await()
        return saved
    }

    /** Elimina el documento de Firestore. */
    override suspend fun delete(id: String) { col.document(id).delete().await() }

    /** Actualiza solo el campo status del producto. */
    override suspend fun setStatus(id: String, status: ProductStatus) { col.document(id).update("status", status.name).await() }

    /** Descuenta el stock de cada producto comprado con un incremento atómico. */
    override suspend fun decrementStock(items: List<OrderItem>) {
        // FieldValue.increment es atómico en el servidor: evita pisar compras simultáneas de otros usuarios.
        items.forEach { col.document(it.productId).update("stock", FieldValue.increment(-it.qty.toLong())).await() }
    }
}

// ====================================================================================== Carrito
/** Carrito por usuario: cada producto es un documento `users/{uid}/cart/{productId}`. */
class FirestoreCartRepo(private val db: FirebaseFirestore) : CartRepository {

    /** Subcolección del carrito de un usuario. */
    private fun col(uid: String) = db.collection("users").document(uid).collection("cart")

    override fun observe(uid: String): Flow<List<CartItem>> = col(uid).observe { it.toCartItem() }

    /** Agrega al carrito sumando cantidad y respetando el stock. */
    override suspend fun add(uid: String, product: Product, qty: Int) {
        if (product.stock <= 0) throw AppException("Producto agotado")
        val ref = col(uid).document(product.id)
        val current = ref.get().await().toCartItem()?.qty ?: 0
        val newQty = (current + qty).coerceAtMost(product.stock)
        val item = CartItem(product.id, product.name, product.price, newQty, product.stock, product.category, product.sellerId, product.sellerName, product.imageUrl)
        ref.set(item.toMap()).await()
    }

    /** Actualiza la cantidad limitándola entre 1 y el stock guardado. */
    override suspend fun setQty(uid: String, productId: String, qty: Int) {
        val ref = col(uid).document(productId)
        val item = ref.get().await().toCartItem() ?: return
        ref.update("qty", qty.coerceIn(1, maxOf(1, item.stock))).await()
    }

    /** Elimina una línea del carrito. */
    override suspend fun remove(uid: String, productId: String) { col(uid).document(productId).delete().await() }

    /** Vacía el carrito en un solo lote (batch). */
    override suspend fun clear(uid: String) {
        val docs = col(uid).get().await().documents
        db.runBatch { batch -> docs.forEach { batch.delete(it.reference) } }.await()
    }
}

// ====================================================================================== Órdenes
/** Órdenes en `orders`. Las consultas por comprador/vendedor usan un solo filtro para no requerir índices compuestos. */
class FirestoreOrderRepo(private val db: FirebaseFirestore) : OrderRepository {

    private val col get() = db.collection("orders")

    /** Crea la orden con un id generado por Firestore y lo devuelve. */
    override suspend fun create(order: Order): String {
        val ref = col.document()
        ref.set(order.copy(id = ref.id).toMap()).await()
        return ref.id
    }

    // Se ordena en el cliente (más recientes primero) para evitar índices compuestos en Firestore.
    override fun observeForBuyer(uid: String): Flow<List<Order>> = col.whereEqualTo("buyerId", uid).observe { it.toOrder() }
    /** Órdenes cuyo arreglo sellerIds contiene al vendedor. */
    override fun observeForSeller(uid: String): Flow<List<Order>> = col.whereArrayContains("sellerIds", uid).observe { it.toOrder() }
    override fun observeAll(): Flow<List<Order>> = col.observe { it.toOrder() }

    /** Actualiza solo el estado de la orden. */
    override suspend fun updateStatus(id: String, status: OrderStatus) { col.document(id).update("status", status.name).await() }

    /** Guarda referencia, estado de la pasarela, modo y nuevo estado de la orden. */
    override suspend fun setPayment(id: String, ref: String, state: String, mode: String, status: OrderStatus) {
        col.document(id).update(mapOf("paymentRef" to ref, "paymentState" to state, "paymentMode" to mode, "status" to status.name)).await()
    }
}

// ====================================================================================== Imágenes
/**
 * Sube imágenes a Firebase Storage y devuelve su URL de descarga.
 * Plan B: si Storage no está disponible (p. ej. el proyecto aún no activó el bucket) guarda una versión reducida
 * de la imagen como data-URI dentro del documento de Firestore, para que la app siga funcionando.
 */
class FirebaseImageStorage(private val storage: FirebaseStorage?) : ImageStorage {
    override suspend fun upload(path: String, bytes: ByteArray): String = try {
        val ref = (storage ?: error("Firebase Storage no está configurado")).reference.child(path)
        ref.putBytes(bytes, StorageMetadata.Builder().setContentType("image/jpeg").build()).await()
        ref.downloadUrl.await().toString()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w("FirebaseImageStorage", "Storage no disponible, se guarda la imagen en Firestore: ${e.message}")
        ImageUtils.toDataUri(bytes)
    }
}
