package com.tramas.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tramas.app.R
import com.tramas.app.data.Services
import com.tramas.app.databinding.ActivityForgotPasswordBinding
import com.tramas.app.ui.common.friendly
import com.tramas.app.ui.common.snack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Recuperación de contraseña: Firebase envía al correo un enlace para crear una contraseña nueva. */
class ForgotPasswordActivity : AppCompatActivity() {

    /** Valida el correo y pide a Firebase el envío del enlace de recuperación. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }
        b.btnBack.setOnClickListener { finish() }

        b.btnSend.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()
            b.tilEmail.error = if (ok) null else getString(R.string.error_email)
            if (!ok) return@setOnClickListener
            if (!Services.ready) return@setOnClickListener b.root.snack(getString(R.string.firebase_missing_title))
            b.btnSend.isEnabled = false
            lifecycleScope.launch {
                try {
                    Services.auth.sendPasswordReset(email)
                    b.cardSuccess.isVisible = true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    b.root.snack(e.friendly())
                } finally {
                    b.btnSend.isEnabled = true
                }
            }
        }
    }
}
