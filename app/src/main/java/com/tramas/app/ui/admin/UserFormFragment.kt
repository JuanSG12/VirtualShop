package com.tramas.app.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Role
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.data.User
import com.tramas.app.databinding.FragmentUserFormBinding
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.snack

/**
 * Crear / editar / eliminar usuario (CRUD completo con persistencia en Firestore).
 * Al CREAR se registra también la cuenta en Firebase Authentication (con la contraseña inicial indicada).
 * Al EDITAR el correo no se puede cambiar; se pueden modificar nombre, teléfono, rol y estado (activo/suspendido).
 */
class UserFormFragment : Fragment() {

    private var _b: FragmentUserFormBinding? = null
    private val b get() = _b!!
    private var existing: User? = null

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentUserFormBinding.inflate(i, c, false)
        return b.root
    }

    /** Prepara el formulario en modo crear o editar según el argumento userId. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val userId = arguments?.getString("userId")
        b.ddRole.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Role.values().map { it.label }))
        val bar = (activity as AppCompatActivity).supportActionBar

        if (userId == null) {
            bar?.title = getString(R.string.new_user)
            b.ddRole.setText(Role.BUYER.label, false)
            b.btnDelete.isVisible = false
        } else {
            bar?.title = getString(R.string.edit_user)
            b.tilPassword.isVisible = false
            b.etEmail.isEnabled = false
            launchSafe {
                val u = Services.users.get(userId) ?: run { b.root.snack(getString(R.string.user_not_found)); findNavController().popBackStack(); return@launchSafe }
                existing = u
                b.tvInitial.text = u.name.firstOrNull()?.toString() ?: "?"
                b.etName.setText(u.name); b.etEmail.setText(u.email); b.etPhone.setText(u.phone)
                b.ddRole.setText(u.role.label, false); b.switchActive.isChecked = u.active
            }
        }

        b.btnSave.setOnClickListener { save() }
        b.btnDelete.setOnClickListener {
            val u = existing ?: return@setOnClickListener
            if (u.id == Session.uid) { b.root.snack(getString(R.string.cannot_delete_self)); return@setOnClickListener }
            requireContext().confirm(R.string.delete_user_title, getString(R.string.delete_user_message, u.name), R.string.delete) {
                launchSafe(onDone = { findNavController().popBackStack() }) { Services.users.delete(u.id) }
            }
        }
    }

    /** Valida y guarda: crea la cuenta (Auth + perfil) o actualiza el perfil existente. */
    private fun save() {
        val name = b.etName.text.toString().trim()
        val email = b.etEmail.text.toString().trim()
        val phone = b.etPhone.text.toString().trim()
        val password = b.etPassword.text.toString()
        val role = Role.values().firstOrNull { it.label == b.ddRole.text.toString() } ?: Role.BUYER
        val u = existing

        b.tilName.error = if (name.length >= 3) null else getString(R.string.error_name)
        b.tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else getString(R.string.error_email)
        b.tilPassword.error = if (u != null || password.length >= 6) null else getString(R.string.error_password_short)
        if (listOf(b.tilName, b.tilEmail, b.tilPassword).any { it.error != null }) return
        if (u != null && u.id == Session.uid && (role != u.role || !b.switchActive.isChecked)) {
            b.root.snack(getString(R.string.cannot_change_self)); return
        }

        launchSafe(b.btnSave, onDone = { _b?.root?.snack(getString(R.string.user_saved)); findNavController().popBackStack() }) {
            if (u != null) {
                u.name = name; u.phone = phone; u.role = role; u.active = b.switchActive.isChecked
                Services.users.save(u)
            } else {
                Services.users.createWithAuth(name, email, phone, password, role, b.switchActive.isChecked)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
