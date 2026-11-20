package com.tramas.app

import android.app.Application
import com.tramas.app.data.firebase.FirebaseServices

/**
 * Clase Application: se ejecuta una vez al abrir la app. Aquí se conectan los repositorios de Firebase al
 * localizador de servicios. Es `open` para que las pruebas la reemplacen por una versión con datos simulados.
 */
open class TramasApp : Application() {
    /** Se ejecuta al abrir la app: instala los servicios (Firebase). */
    override fun onCreate() {
        super.onCreate()
        installServices()
    }

    /** Instala las implementaciones reales (Firebase). Las pruebas sobrescriben este método. */
    protected open fun installServices() {
        FirebaseServices.install(this)
    }
}
