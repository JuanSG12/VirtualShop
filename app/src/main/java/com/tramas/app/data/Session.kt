package com.tramas.app.data

/** Sesión en memoria: rol activo y nombre mostrado en el menú lateral. */
object Session {
    var role: Role = Role.BUYER
    var name: String = "Juan Pérez"
    var email: String = "juan@correo.com"

    fun start(role: Role, email: String) {
        this.role = role
        this.email = email
        name = when (role) {
            Role.BUYER -> "Juan Pérez"
            Role.SELLER -> "Taller Kai"
            Role.ADMIN -> "Sofía Mora"
        }
    }
}
