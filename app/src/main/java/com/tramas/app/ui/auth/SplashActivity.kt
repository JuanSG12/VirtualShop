package com.tramas.app.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.ActivitySplashBinding
import com.tramas.app.ui.host.openHome
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida: anima el logo y decide a dónde ir.
 * Si el dispositivo conserva una sesión de Firebase (el usuario no cerró sesión) entra directo a su interfaz; si no, al login.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    /** Anima el logo y, tras 2 s, abre el login o directamente la interfaz del rol si hay sesión. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.imgLogo.apply { alpha = 0f; scaleX = 0.6f; scaleY = 0.6f; animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700).start() }
        listOf(b.tvBrand, b.tvTagline).forEachIndexed { i, v ->
            v.alpha = 0f; v.translationY = 24f
            v.animate().alpha(1f).translationY(0f).setStartDelay(350L + i * 150).setDuration(500).start()
        }

        lifecycleScope.launch {
            delay(2000)
            val user = if (Services.ready) runCatching { Services.auth.loadCurrentProfile() }.getOrNull() else null
            if (user != null) {
                Session.user = user
                openHome(user.role)
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}
