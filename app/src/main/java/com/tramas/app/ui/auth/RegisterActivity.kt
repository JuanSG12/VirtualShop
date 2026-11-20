package com.tramas.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tramas.app.R
import com.tramas.app.data.Role
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.ActivityRegisterBinding
import com.tramas.app.ui.common.friendly
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.openHome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Registro de cuenta nueva como COMPRADOR o VENDEDOR (el administrador lo asigna otro administrador). */
class RegisterActivity : AppCompatActivity() {

    /** Valida el formulario y registra la cuenta (Authentication + perfil en Firestore). */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }

        b.btnRegister.setOnClickListener {
            val name = b.etName.text.toString().trim()
            val email = b.etEmail.text.toString().trim()
            val phone = b.etPhone.text.toString().trim()
            val password = b.etPassword.text.toString()
            b.tilName.error = if (name.length >= 3) null else getString(R.string.error_name)
            b.tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else getString(R.string.error_email)
            b.tilPhone.error = if (phone.length >= 7) null else getString(R.string.error_phone)
            b.tilPassword.error = if (password.length >= 8) null else getString(R.string.error_password_short8)
            if (listOf(b.tilName, b.tilEmail, b.tilPhone, b.tilPassword).any { it.error != null }) return@setOnClickListener
            if (!b.cbConsent.isChecked) return@setOnClickListener b.root.snack(getString(R.string.error_consent))
            if (!Services.ready) return@setOnClickListener b.root.snack(getString(R.string.firebase_missing_title))

            val role = if (b.toggleRole.checkedButtonId == R.id.btnRoleSeller) Role.SELLER else Role.BUYER
            b.btnRegister.isEnabled = false
            lifecycleScope.launch {
                try {
                    val user = Services.auth.register(name, email, phone, password, role)
                    Session.user = user
                    openHome(user.role)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    b.btnRegister.isEnabled = true
                    b.root.snack(e.friendly())
                }
            }
        }
    }
}
