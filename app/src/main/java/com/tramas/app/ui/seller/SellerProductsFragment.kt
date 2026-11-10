package com.tramas.app.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Product
import com.tramas.app.data.ProductStatus
import com.tramas.app.data.Repo
import com.tramas.app.data.Session
import com.tramas.app.databinding.FragmentSellerProductsBinding
import com.tramas.app.databinding.ItemSellerProductBinding
import com.tramas.app.databinding.ViewStatCardBinding
import com.tramas.app.ui.common.BindingAdapter
import com.tramas.app.ui.common.badge
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.cop
import com.tramas.app.ui.common.show
import com.tramas.app.ui.common.snack

/** Panel de productos del vendedor: indicadores + listado con editar / eliminar (CRUD) y botón de crear. */
class SellerProductsFragment : Fragment() {

    private var _b: FragmentSellerProductsBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: BindingAdapter<Product, ItemSellerProductBinding>

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSellerProductsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BindingAdapter(ItemSellerProductBinding::inflate) { vb, p ->
            vb.imgArt.setImageResource(p.art)
            p.photo?.let { vb.imgArt.setImageBitmap(it) }
            vb.tvName.text = p.name
            vb.tvPrice.text = p.price.cop()
            vb.tvStock.text = getString(R.string.stock_category, p.stock, p.category)
            vb.tvStatus.badge(p.status.label, p.status.color)
            vb.btnEdit.setOnClickListener { findNavController().navigate(R.id.productFormFragment, bundleOf("productId" to p.id)) }
            vb.btnDelete.setOnClickListener {
                requireContext().confirm(R.string.delete_product_title, getString(R.string.delete_product_message, p.name), R.string.delete) {
                    Repo.deleteProduct(p.id); refresh(); b.root.snack(getString(R.string.product_deleted))
                }
            }
        }
        b.rvList.adapter = adapter
        b.fabAdd.setOnClickListener { findNavController().navigate(R.id.productFormFragment, bundleOf("productId" to -1)) }
        b.etSearch.doAfterTextChanged { refresh() }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val mine = Repo.sellerProducts(Session.name)
        stat(b.statActive, R.drawable.ic_check, mine.count { it.status == ProductStatus.ACTIVE }, R.string.stat_active)
        stat(b.statLow, R.drawable.ic_info, mine.count { it.stock <= 5 }, R.string.stat_low_stock)
        stat(b.statPending, R.drawable.ic_pending, mine.count { it.status == ProductStatus.PENDING }, R.string.stat_pending)
        val q = b.etSearch.text.toString().trim()
        val list = mine.filter { it.name.contains(q, true) }
        adapter.items = list
        b.emptyState.show(list.isEmpty(), getString(R.string.no_products), getString(R.string.no_products_hint))
    }

    private fun stat(s: ViewStatCardBinding, icon: Int, value: Int, label: Int) {
        s.imgIcon.setImageResource(icon); s.tvValue.text = value.toString(); s.tvLabel.setText(label)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
