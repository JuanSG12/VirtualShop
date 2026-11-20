package com.tramas.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.tramas.app.R
import com.tramas.app.data.Role
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.data.User
import com.tramas.app.databinding.FragmentAdminUsersBinding
import com.tramas.app.databinding.ItemUserBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadAvatar
import com.tramas.app.ui.common.show
import com.tramas.app.ui.common.snack

/** Gestión de usuarios: listado en tiempo real (Firestore) con búsqueda y filtro por rol; crear, editar y eliminar. */
class AdminUsersFragment : Fragment() {

    private var _b: FragmentAdminUsersBinding? = null
    private val b get() = _b!!
    private var filter: Role? = null
    private var users: List<User> = emptyList()
    private lateinit var adapter: BindingAdapter<User, ItemUserBinding>

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminUsersBinding.inflate(i, c, false)
        return b.root
    }

    /** Configura el listado, los filtros por rol y escucha los usuarios de Firestore. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BindingAdapter(ItemUserBinding::inflate) { vb, u ->
            vb.tvInitial.text = u.name.firstOrNull()?.toString() ?: "?"
            vb.tvName.text = u.name
            vb.tvEmail.text = u.email
            vb.tvRole.badge(u.role.label, when (u.role) { Role.ADMIN -> R.color.indigo; Role.SELLER -> R.color.terracotta; Role.BUYER -> R.color.teal })
            vb.tvStatus.badge(getString(if (u.active) R.string.active else R.string.suspended), if (u.active) R.color.success else R.color.danger)
            val edit = { findNavController().navigate(R.id.userFormFragment, bundleOf("userId" to u.id)) }
            vb.root.setOnClickListener { edit() }
            vb.btnEdit.setOnClickListener { edit() }
            vb.btnDelete.setOnClickListener {
                if (u.id == Session.uid) { b.root.snack(getString(R.string.cannot_delete_self)); return@setOnClickListener }
                requireContext().confirm(R.string.delete_user_title, getString(R.string.delete_user_message, u.name), R.string.delete) {
                    launchSafe { Services.users.delete(u.id); b.root.snack(getString(R.string.user_deleted)) }
                }
            }
        }
        b.rvList.adapter = adapter

        listOf<Role?>(null, Role.BUYER, Role.SELLER, Role.ADMIN).forEach { r ->
            b.chipGroup.addView(Chip(requireContext()).apply {
                id = View.generateViewId()
                text = r?.let { it.label + "s" } ?: getString(R.string.all)
                isCheckable = true; isChecked = r == null
                setOnClickListener { filter = r; render() }
            })
        }
        b.etSearch.doAfterTextChanged { render() }
        b.fabAdd.setOnClickListener { findNavController().navigate(R.id.userFormFragment) }
        collectFlow(Services.users.observeAll()) { users = it.sortedBy { u -> u.name.lowercase() }; render() }
    }

    /** Aplica búsqueda y filtro de rol sobre la lista de usuarios. */
    private fun render() {
        if (_b == null) return
        val q = b.etSearch.text.toString().trim()
        val list = users.filter { (filter == null || it.role == filter) && (it.name.contains(q, true) || it.email.contains(q, true)) }
        adapter.items = list
        b.emptyState.show(list.isEmpty(), getString(R.string.no_users), getString(R.string.no_users_hint))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
