package com.tramas.app.ui.buyer

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentBuyerProfileBinding
import com.tramas.app.security.BiometricVault
import com.tramas.app.ui.common.BiometricHelper
import com.tramas.app.ui.common.ImagePicker
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.friendly
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadAvatar
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.RoleHostActivity
import kotlinx.coroutines.launch

/**
 * Perfil del comprador (CRUD de perfil): editar datos, foto de perfil (cámara/galería -> Firebase Storage),
 * dirección con geolocalización, ingreso con huella y eliminar cuenta.
 */
class BuyerProfileFragment : Fragment() {

    private var _b: FragmentBuyerProfileBinding? = null
    private val b get() = _b!!
    private var lat: Double? = null
    private var lng: Double? = null
    private lateinit var vault: BiometricVault

    /** Sube la foto elegida y guarda su URL en el perfil. */
    private val picker = ImagePicker(this) { bytes ->
        launchSafe {
            val user = Session.user ?: return@launchSafe
            user.photoUrl = Services.images.upload("users/${user.id}/avatar.jpg", bytes)
            Services.users.save(user)
            _b?.imgAvatar?.loadAvatar(user.photoUrl)
            _b?.root?.snack(getString(R.string.photo_updated))
        }
    }

    private val location = LocationHelper(this) { p ->
        val bind = _b ?: return@LocationHelper
        if (p == null) { bind.root.snack(getString(R.string.location_unavailable)); return@LocationHelper }
        lat = p.lat; lng = p.lng
        bind.etAddress.setText(p.address.ifBlank { "%.5f, %.5f".format(p.lat, p.lng) })
    }

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentBuyerProfileBinding.inflate(i, c, false)
        return b.root
    }

    /** Llena el perfil con los datos del usuario y conecta foto, GPS, guardado, huella y cuenta. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vault = BiometricVault(requireContext())
        val user = Session.user ?: return
        lat = user.lat; lng = user.lng
        b.tvName.text = user.name
        b.tvEmail.text = user.email
        b.etName.setText(user.name)
        b.etPhone.setText(user.phone)
        b.etAddress.setText(user.address)
        b.imgAvatar.loadAvatar(user.photoUrl)

        b.btnCamera.setOnClickListener { picker.show() }
        b.btnLocation.setOnClickListener { location.request() }
        b.btnSave.setOnClickListener {
            launchSafe(b.btnSave) {
                user.name = b.etName.text.toString().trim().ifBlank { user.name }
                user.phone = b.etPhone.text.toString().trim()
                user.address = b.etAddress.text.toString().trim()
                user.lat = lat; user.lng = lng
                Services.users.save(user)
                b.tvName.text = user.name
                b.root.snack(getString(R.string.profile_saved))
            }
        }

        // Preferencias de notificación: se guardan en el dispositivo.
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        b.switchNotifications.isChecked = prefs.getBoolean("notif_orders", true)
        b.switchNotifications.setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean("notif_orders", on).apply() }
        b.switchPromos.isChecked = prefs.getBoolean("notif_promos", false)
        b.switchPromos.setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean("notif_promos", on).apply() }

        setupFingerprintSwitch(user.email)

        b.btnLogout.setOnClickListener { (activity as RoleHostActivity).confirmLogout() }
        b.btnDelete.setOnClickListener {
            requireContext().confirm(R.string.delete_account_title, getString(R.string.delete_account_message), R.string.delete) { deleteAccount() }
        }
    }

    /** Interruptor de huella: al activar pide la contraseña (se verifica) y la protege con la huella; al desactivar borra la bóveda. */
    private fun setupFingerprintSwitch(email: String) {
        b.switchBiometric.isChecked = vault.isEnabled()
        b.switchBiometric.setOnCheckedChangeListener { button, on ->
            if (!button.isPressed) return@setOnCheckedChangeListener
            if (!on) { vault.clear(); b.root.snack(getString(R.string.biometric_disabled)); return@setOnCheckedChangeListener }
            if (!BiometricHelper.isAvailable(requireActivity())) {
                b.switchBiometric.isChecked = false; b.root.snack(getString(R.string.biometric_unavailable_message)); return@setOnCheckedChangeListener
            }
            val input = EditText(requireContext()).apply { hint = getString(R.string.password); inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.biometric_enable_title).setMessage(R.string.confirm_password_message).setView(input)
                .setPositiveButton(R.string.activate) { _, _ -> enableFingerprint(email, input.text.toString()) }
                .setNegativeButton(R.string.cancel) { _, _ -> b.switchBiometric.isChecked = false }
                .setOnCancelListener { b.switchBiometric.isChecked = false }
                .show()
        }
    }

    /** Verifica la contraseña (inicio de sesión) y activa la huella protegiendo la contraseña con el Keystore. */
    private fun enableFingerprint(email: String, password: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Services.auth.signIn(email, password) // comprueba que la contraseña sea la correcta
                BiometricHelper.enroll(requireActivity(), vault, email, password) { ok, error ->
                    _b?.switchBiometric?.isChecked = ok
                    if (!ok && !error.isNullOrBlank()) _b?.root?.snack(error)
                }
            } catch (e: Exception) {
                _b?.switchBiometric?.isChecked = false
                _b?.root?.snack(e.friendly())
            }
        }
    }

    /** Elimina el perfil y la cuenta de Firebase (derecho de supresión de datos) y vuelve al login. */
    private fun deleteAccount() = launchSafe {
        Services.auth.deleteOwnAccount()
        vault.clear()
        (activity as RoleHostActivity).returnToLogin()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
