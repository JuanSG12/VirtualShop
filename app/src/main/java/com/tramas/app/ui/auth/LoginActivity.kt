package com.tramas.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.ActivityLoginBinding
import com.tramas.app.security.BiometricVault
import com.tramas.app.ui.common.BiometricHelper
import com.tramas.app.ui.common.friendly
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.openHome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Inicio de sesión para los tres roles. El rol NO se elige aquí: se lee del perfil guardado en Firestore y de ahí
 * se decide qué interfaz abrir. Admite dos formas de entrar: correo + contraseña, o huella dactilar.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var vault: BiometricVault

    /** Conecta los botones de ingreso, huella, registro y recuperación; avisa si falta configurar Firebase. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        vault = BiometricVault(this)

        b.btnLogin.setOnClickListener { if (validate()) signIn(b.etEmail.text.toString(), b.etPassword.text.toString(), offerFingerprint = true) }
        b.btnBiometric.setOnClickListener { fingerprintLogin() }
        b.btnForgot.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        b.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        vault.savedEmail()?.let { if (b.etEmail.text.isNullOrBlank()) b.etEmail.setText(it) }

        if (!Services.ready) {
            MaterialAlertDialogBuilder(this).setTitle(R.string.firebase_missing_title)
                .setMessage(R.string.firebase_missing_message).setPositiveButton(R.string.ok, null).show()
        }
    }

    /** Valida el formato del correo y la longitud de la contraseña antes de llamar al servidor. */
    private fun validate(): Boolean {
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(b.etEmail.text.toString().trim()).matches()
        b.tilEmail.error = if (emailOk) null else getString(R.string.error_email)
        val passOk = b.etPassword.text.toString().length >= 6
        b.tilPassword.error = if (passOk) null else getString(R.string.error_password_short)
        return emailOk && passOk
    }

    /**
     * Autentica contra Firebase y abre la interfaz del rol.
     * @param offerFingerprint si es true y el equipo tiene huella, ofrece activarla tras entrar con contraseña.
     */
    private fun signIn(email: String, password: String, offerFingerprint: Boolean) {
        if (!Services.ready) { b.root.snack(getString(R.string.firebase_missing_title)); return }
        setLoading(true)
        lifecycleScope.launch {
            try {
                val user = Services.auth.signIn(email, password)
                Session.user = user
                if (offerFingerprint && BiometricHelper.isAvailable(this@LoginActivity) && vault.savedEmail() != email.trim()) {
                    askEnableFingerprint(email.trim(), password) { openHome(user.role) }
                } else openHome(user.role)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setLoading(false)
                b.root.snack(e.friendly())
            }
        }
    }

    /** Pregunta si desea activar el ingreso con huella; si acepta, pide la huella y guarda la contraseña protegida. */
    private fun askEnableFingerprint(email: String, password: String, then: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.biometric_enable_title)
            .setMessage(R.string.biometric_enable_message)
            .setPositiveButton(R.string.activate) { _, _ ->
                BiometricHelper.enroll(this, vault, email, password) { ok, error ->
                    if (!ok && !error.isNullOrBlank()) b.root.snack(error)
                    then()
                }
            }
            .setNegativeButton(R.string.not_now) { _, _ -> then() }
            .setCancelable(false)
            .show()
    }

    /** Ingreso con huella: recupera la contraseña guardada (cifrada) y entra con ella. */
    private fun fingerprintLogin() {
        if (!BiometricHelper.isAvailable(this)) {
            MaterialAlertDialogBuilder(this).setTitle(R.string.biometric_unavailable_title)
                .setMessage(R.string.biometric_unavailable_message).setPositiveButton(R.string.ok, null).show()
            return
        }
        if (!vault.isEnabled()) {
            MaterialAlertDialogBuilder(this).setTitle(R.string.biometric_not_enabled_title)
                .setMessage(R.string.biometric_not_enabled_message).setPositiveButton(R.string.ok, null).show()
            return
        }
        BiometricHelper.login(this, vault,
            onPassword = { email, password -> signIn(email, password, offerFingerprint = false) },
            onError = { if (it.isNotBlank()) b.root.snack(it) })
    }

    /** Bloquea los botones y cambia el texto mientras se autentica. */
    private fun setLoading(loading: Boolean) {
        b.btnLogin.isEnabled = !loading
        b.btnBiometric.isEnabled = !loading
        b.btnLogin.setText(if (loading) R.string.signing_in else R.string.sign_in)
    }
}
