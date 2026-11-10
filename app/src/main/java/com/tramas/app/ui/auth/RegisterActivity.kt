package com.tramas.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.tramas.app.R
import com.tramas.app.data.Repo
import com.tramas.app.data.Role
import com.tramas.app.databinding.ActivityRegisterBinding
import com.tramas.app.ui.common.snack

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }

        b.btnRegister.setOnClickListener {
            val name = b.etName.text.toString().trim()
            val email = b.etEmail.text.toString().trim()
            val phone = b.etPhone.text.toString().trim()
            b.tilName.error = if (name.length >= 3) null else getString(R.string.error_name)
            b.tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else getString(R.string.error_email)
            b.tilPhone.error = if (phone.length >= 7) null else getString(R.string.error_phone)
            b.tilPassword.error = if (b.etPassword.text.toString().length >= 8) null else getString(R.string.error_password_short8)
            if (listOf(b.tilName, b.tilEmail, b.tilPhone, b.tilPassword).any { it.error != null }) return@setOnClickListener
            if (!b.cbConsent.isChecked) return@setOnClickListener b.root.snack(getString(R.string.error_consent))

            val role = if (b.toggleRole.checkedButtonId == R.id.btnRoleSeller) Role.SELLER else Role.BUYER
            Repo.addUser(name, email, phone, role, true)
            b.root.snack(getString(R.string.account_created))
            b.root.postDelayed({ finish() }, 900)
        }
    }
}
