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
import com.tramas.app.data.Repo
import com.tramas.app.data.Role
import com.tramas.app.databinding.FragmentUserFormBinding
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.snack

/** Crear / editar / eliminar usuario (CRUD completo desde el panel de administración). */
class UserFormFragment : Fragment() {

    private var _b: FragmentUserFormBinding? = null
    private val b get() = _b!!
    private var userId = -1

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentUserFormBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userId = requireArguments().getInt("userId", -1)
        b.ddRole.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Role.values().map { it.label }))
        val bar = (activity as AppCompatActivity).supportActionBar
        val user = Repo.user(userId)
        if (user != null) {
            bar?.title = getString(R.string.edit_user)
            b.tvInitial.text = user.name.first().toString()
            b.etName.setText(user.name); b.etEmail.setText(user.email); b.etPhone.setText(user.phone)
            b.ddRole.setText(user.role.label, false); b.switchActive.isChecked = user.active
        } else {
            bar?.title = getString(R.string.new_user)
            b.ddRole.setText(Role.BUYER.label, false)
            b.btnDelete.isVisible = false
        }

        b.btnSave.setOnClickListener { save() }
        b.btnDelete.setOnClickListener {
            requireContext().confirm(R.string.delete_user_title, getString(R.string.delete_user_message, user?.name ?: ""), R.string.delete) {
                Repo.deleteUser(userId); b.root.snack(getString(R.string.user_deleted)); findNavController().popBackStack()
            }
        }
    }

    private fun save() {
        val name = b.etName.text.toString().trim()
        val email = b.etEmail.text.toString().trim()
        b.tilName.error = if (name.length >= 3) null else getString(R.string.error_name)
        b.tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else getString(R.string.error_email)
        if (b.tilName.error != null || b.tilEmail.error != null) return

        val role = Role.values().firstOrNull { it.label == b.ddRole.text.toString() } ?: Role.BUYER
        val phone = b.etPhone.text.toString().trim()
        val existing = Repo.user(userId)
        if (existing != null) {
            existing.name = name; existing.email = email; existing.phone = phone; existing.role = role; existing.active = b.switchActive.isChecked
        } else Repo.addUser(name, email, phone, role, b.switchActive.isChecked)
        b.root.snack(getString(R.string.user_saved))
        findNavController().popBackStack()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
