package com.tramas.app.data

/**
 * Sesión en memoria: guarda el perfil del usuario autenticado y su última ubicación conocida.
 * Se llena al iniciar sesión (LoginActivity) y se limpia al cerrar sesión.
 */
object Session {
    /** Perfil del usuario con sesión iniciada; null si no hay sesión. */
    var user: User? = null

    /** Última ubicación capturada (checkout, perfil o inicio); sirve para calcular distancias a los productos. */
    var location: GeoPoint? = null

    val role: Role get() = user?.role ?: Role.BUYER
    val uid: String get() = user?.id ?: ""
    val name: String get() = user?.name ?: ""
    val email: String get() = user?.email ?: ""

    /** Limpia todo al cerrar sesión. */
    fun clear() { user = null; location = null }
}
