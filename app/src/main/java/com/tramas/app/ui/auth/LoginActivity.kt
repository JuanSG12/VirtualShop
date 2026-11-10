package com.tramas.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Role
import com.tramas.app.data.Session
import com.tramas.app.databinding.ActivityLoginBinding
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.AdminActivity
import com.tramas.app.ui.host.BuyerActivity
import com.tramas.app.ui.host.SellerActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.setOnClickListener { if (validate()) openHome(b.etEmail.text.toString()) }
        b.btnBiometric.setOnClickListener { authenticateWithFingerprint() }
        b.btnForgot.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        b.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun validate(): Boolean {
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(b.etEmail.text.toString().trim()).matches()
        b.tilEmail.error = if (emailOk) null else getString(R.string.error_email)
        val passOk = b.etPassword.text.toString().length >= 6
        b.tilPassword.error = if (passOk) null else getString(R.string.error_password_short)
        return emailOk && passOk
    }

    private fun selectedRole() = when (b.chipRole.checkedChipId) {
        R.id.chipSeller -> Role.SELLER
        R.id.chipAdmin -> Role.ADMIN
        else -> Role.BUYER
    }

    private fun openHome(email: String) {
        val role = selectedRole()
        Session.start(role, email.ifBlank { "demo@tramas.co" })
        val target = when (role) {
            Role.BUYER -> BuyerActivity::class.java
            Role.SELLER -> SellerActivity::class.java
            Role.ADMIN -> AdminActivity::class.java
        }
        startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    /** Ingreso con huella: BiometricPrompt del sistema; si el equipo no tiene huella, ofrece modo demostración. */
    private fun authenticateWithFingerprint() {
        val status = BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.biometric_unavailable_title)
                .setMessage(R.string.biometric_unavailable_message)
                .setPositiveButton(R.string.continue_demo) { _, _ -> openHome(b.etEmail.text.toString()) }
                .setNegativeButton(R.string.cancel, null)
                .show()
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = openHome(b.etEmail.text.toString())
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON && errorCode != BiometricPrompt.ERROR_USER_CANCELED) b.root.snack(errString.toString())
            }
            override fun onAuthenticationFailed() = b.root.snack(getString(R.string.biometric_failed))
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.use_password))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }
}
