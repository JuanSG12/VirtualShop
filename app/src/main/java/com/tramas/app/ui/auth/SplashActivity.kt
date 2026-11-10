package com.tramas.app.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.tramas.app.databinding.ActivitySplashBinding

/** Pantalla de bienvenida: anima el logo y pasa al login. */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.imgLogo.apply { alpha = 0f; scaleX = 0.6f; scaleY = 0.6f; animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700).start() }
        listOf(b.tvBrand, b.tvTagline).forEachIndexed { i, v ->
            v.alpha = 0f; v.translationY = 24f
            v.animate().alpha(1f).translationY(0f).setStartDelay(350L + i * 150).setDuration(500).start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2200)
    }
}
