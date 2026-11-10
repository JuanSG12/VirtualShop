package com.tramas.app.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tramas.app.R
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentSellerProfileBinding
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.registerCamera
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.RoleHostActivity

/** Perfil del vendedor: logo con cámara, datos del negocio, ubicación por GPS y radio de entrega. */
class SellerProfileFragment : Fragment() {

    private var _b: FragmentSellerProfileBinding? = null
    private val b get() = _b!!
    private val camera = registerCamera { bmp -> _b?.imgLogo?.apply { imageTintList = null; setPadding(0, 0, 0, 0); setImageBitmap(bmp) } }
    private val location = LocationHelper(this) { t -> _b?.tvLocation?.text = t }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSellerProfileBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvShopName.text = Session.name
        b.etBusiness.setText(Session.name)
        b.etNit.setText("900.123.456-7")
        b.etPhone.setText("311 222 3344")
        b.etCraft.setText("Tejeduría wayúu")
        b.tvRadius.text = getString(R.string.delivery_radius, b.sliderRadius.value.toInt())
        b.sliderRadius.addOnChangeListener { _, v, _ -> b.tvRadius.text = getString(R.string.delivery_radius, v.toInt()) }
        b.btnLogo.setOnClickListener { camera.launch(null) }
        b.btnLocation.setOnClickListener { location.request() }
        b.btnSave.setOnClickListener {
            Session.name = b.etBusiness.text.toString().ifBlank { Session.name }
            b.tvShopName.text = Session.name
            b.root.snack(getString(R.string.profile_saved))
        }
        b.btnLogout.setOnClickListener { (activity as RoleHostActivity).confirmLogout() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
