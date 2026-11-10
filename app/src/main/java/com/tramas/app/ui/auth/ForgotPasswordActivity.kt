package com.tramas.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.tramas.app.R
import com.tramas.app.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }
        b.btnBack.setOnClickListener { finish() }

        b.btnSend.setOnClickListener {
            val ok = Patterns.EMAIL_ADDRESS.matcher(b.etEmail.text.toString().trim()).matches()
            b.tilEmail.error = if (ok) null else getString(R.string.error_email)
            b.cardSuccess.isVisible = ok
        }
    }
}
