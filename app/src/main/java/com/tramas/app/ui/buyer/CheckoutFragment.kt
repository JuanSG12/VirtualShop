package com.tramas.app.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Repo
import com.tramas.app.databinding.DialogProcessingBinding
import com.tramas.app.databinding.FragmentCheckoutBinding
import com.tramas.app.databinding.RowSummaryBinding
import com.tramas.app.ui.common.LocationHelper
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.snack

/** Pasarela de pagos (maqueta funcional): dirección + geolocalización, método de pago, resumen y confirmación. */
class CheckoutFragment : Fragment() {

    private var _b: FragmentCheckoutBinding? = null
    private val b get() = _b!!
    private val location = LocationHelper(this) { text -> _b?.tvLocation?.text = text }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCheckoutBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        line(b.rowSubtotal, getString(R.string.subtotal), Repo.subtotal())
        line(b.rowIva, getString(R.string.iva), Repo.iva())
        line(b.rowShipping, getString(R.string.shipping), Repo.shipping())
        line(b.rowTotal, getString(R.string.total), Repo.total())
        b.btnPay.text = getString(R.string.pay_amount, Repo.total().cop())

        b.btnLocation.setOnClickListener { location.request() }
        b.rgPayment.setOnCheckedChangeListener { _, id -> b.groupCard.isVisible = id == R.id.rbCard }
        // Formato automático de la fecha de vencimiento: 1225 -> 12/25
        b.etExpiry.doAfterTextChanged { t ->
            val digits = t.toString().filter { it.isDigit() }
            val formatted = if (digits.length > 2) digits.take(2) + "/" + digits.drop(2) else digits
            if (formatted != t.toString()) { b.etExpiry.setText(formatted); b.etExpiry.setSelection(formatted.length) }
        }
        b.btnPay.setOnClickListener { if (validate()) pay() }
    }

    private fun validate(): Boolean {
        b.tilAddress.error = if (b.etAddress.text.toString().trim().length >= 6) null else getString(R.string.error_address)
        var ok = b.tilAddress.error == null
        if (b.rbCard.isChecked) {
            b.tilCardNumber.error = if (b.etCardNumber.text.toString().length >= 13) null else getString(R.string.error_card)
            b.tilExpiry.error = if (b.etExpiry.text.toString().length == 5) null else getString(R.string.error_expiry)
            b.tilCvv.error = if (b.etCvv.text.toString().length >= 3) null else getString(R.string.error_cvv)
            ok = ok && listOf(b.tilCardNumber, b.tilExpiry, b.tilCvv).all { it.error == null }
        }
        if (ok && !b.cbTerms.isChecked) { b.root.snack(getString(R.string.error_terms)); ok = false }
        return ok
    }

    private fun pay() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(DialogProcessingBinding.inflate(layoutInflater).root)
            .setCancelable(false).show()
        b.root.postDelayed({
            if (_b == null) return@postDelayed
            dialog.dismiss()
            val order = Repo.placeOrder()
            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_check)
                .setTitle(R.string.payment_approved)
                .setMessage(getString(R.string.payment_approved_message, order.id, order.total.cop()))
                .setPositiveButton(R.string.view_orders) { _, _ -> findNavController().navigate(R.id.buyerOrdersFragment) }
                .setNegativeButton(R.string.keep_shopping) { _, _ -> findNavController().navigate(R.id.homeFragment) }
                .setCancelable(false).show()
        }, 1800)
    }

    private fun line(row: RowSummaryBinding, label: String, value: Long) {
        row.tvLabel.text = label
        row.tvValue.text = value.cop()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
