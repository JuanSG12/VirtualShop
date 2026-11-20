package com.tramas.app.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tramas.app.R
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentSellerProfileBinding
import com.tramas.app.ui.common.ImagePicker
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.loadAvatar
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.RoleHostActivity

/** Perfil del vendedor: logo (cámara/galería -> Storage), datos del negocio, ubicación del taller por GPS y radio de entrega. */
class SellerProfileFragment : Fragment() {

    private var _b: FragmentSellerProfileBinding? = null
    private val b get() = _b!!
    private var lat: Double? = null
    private var lng: Double? = null
    private var address: String = ""

    private val picker = ImagePicker(this) { bytes ->
        launchSafe {
            val user = Session.user ?: return@launchSafe
            user.photoUrl = Services.images.upload("users/${user.id}/avatar.jpg", bytes)
            Services.users.save(user)
            _b?.imgLogo?.loadAvatar(user.photoUrl)
            _b?.root?.snack(getString(R.string.photo_updated))
        }
    }

    private val location = LocationHelper(this) { p ->
        val bind = _b ?: return@LocationHelper
        if (p == null) { bind.root.snack(getString(R.string.location_unavailable)); return@LocationHelper }
        lat = p.lat; lng = p.lng; address = p.address
        bind.tvLocation.text = p.address.ifBlank { getString(R.string.location_captured, p.lat, p.lng) }
    }

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSellerProfileBinding.inflate(i, c, false)
        return b.root
    }

    /** Llena el perfil con los datos del vendedor y conecta logo, GPS, radio y guardado. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = Session.user ?: return
        lat = user.lat; lng = user.lng; address = user.address
        b.tvShopName.text = user.name
        b.etBusiness.setText(user.name)
        b.etNit.setText(user.nit)
        b.etPhone.setText(user.phone)
        b.etCraft.setText(user.craft)
        b.sliderRadius.value = user.radiusKm.toFloat().coerceIn(5f, 100f)
        b.tvLocation.text = user.address.ifBlank { getString(R.string.no_location_yet) }
        b.imgLogo.loadAvatar(user.photoUrl)
        b.tvRadius.text = getString(R.string.delivery_radius, b.sliderRadius.value.toInt())
        b.sliderRadius.addOnChangeListener { _, v, _ -> b.tvRadius.text = getString(R.string.delivery_radius, v.toInt()) }

        b.btnLogo.setOnClickListener { picker.show() }
        b.btnLocation.setOnClickListener { location.request() }
        b.btnSave.setOnClickListener {
            launchSafe(b.btnSave) {
                user.name = b.etBusiness.text.toString().trim().ifBlank { user.name }
                user.nit = b.etNit.text.toString().trim()
                user.phone = b.etPhone.text.toString().trim()
                user.craft = b.etCraft.text.toString().trim()
                user.radiusKm = b.sliderRadius.value.toInt()
                user.lat = lat; user.lng = lng; user.address = address
                Services.users.save(user)
                b.tvShopName.text = user.name
                b.root.snack(getString(R.string.profile_saved))
            }
        }
        b.btnLogout.setOnClickListener { (activity as RoleHostActivity).confirmLogout() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
