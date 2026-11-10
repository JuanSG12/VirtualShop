package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tramas.app.R
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentBuyerProfileBinding
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.registerCamera
import com.tramas.app.ui.common.snack
import com.tramas.app.ui.host.RoleHostActivity

/** Perfil del comprador: datos editables, foto con cámara, preferencias, cerrar sesión y eliminar cuenta. */
class BuyerProfileFragment : Fragment() {

    private var _b: FragmentBuyerProfileBinding? = null
    private val b get() = _b!!

    private val camera = registerCamera { bmp ->
        _b?.imgAvatar?.apply { imageTintList = null; setPadding(0, 0, 0, 0); setImageBitmap(bmp) }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentBuyerProfileBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvName.text = Session.name
        b.tvEmail.text = Session.email
        b.etName.setText(Session.name)
        b.etPhone.setText("300 111 2233")
        b.etAddress.setText("Cra 7 # 45-12, Bogotá")

        b.btnCamera.setOnClickListener { camera.launch(null) }
        b.btnSave.setOnClickListener {
            Session.name = b.etName.text.toString().ifBlank { Session.name }
            b.tvName.text = Session.name
            b.root.snack(getString(R.string.profile_saved))
        }
        b.btnLogout.setOnClickListener { (activity as RoleHostActivity).confirmLogout() }
        b.btnDelete.setOnClickListener {
            requireContext().confirm(R.string.delete_account_title, getString(R.string.delete_account_message), R.string.delete) {
                (activity as RoleHostActivity).confirmLogout()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
