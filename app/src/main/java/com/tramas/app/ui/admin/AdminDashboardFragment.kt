package com.tramas.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tramas.app.R
import com.tramas.app.data.Order
import com.tramas.app.data.Product
import com.tramas.app.data.ProductStatus
import com.tramas.app.data.Services
import com.tramas.app.data.Session
import com.tramas.app.data.User
import com.tramas.app.databinding.FragmentAdminDashboardBinding
import com.tramas.app.databinding.ViewStatCardBinding
import com.tramas.app.ui.common.collectFlow
import com.tramas.app.ui.common.confirm
import com.tramas.app.ui.common.launchSafe
import com.tramas.app.ui.common.snack
import java.util.Calendar
import java.util.Locale

/** Dashboard del administrador: indicadores, gráfico de la semana y alertas, todo calculado en tiempo real desde Firestore. */
class AdminDashboardFragment : Fragment() {

    private var _b: FragmentAdminDashboardBinding? = null
    private val b get() = _b!!
    private var users: List<User> = emptyList()
    private var products: List<Product> = emptyList()
    private var orders: List<Order> = emptyList()

    /** Infla el layout de la pantalla con ViewBinding. */
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAdminDashboardBinding.inflate(i, c, false)
        return b.root
    }

    /** Conecta los botones y escucha en tiempo real usuarios, productos y órdenes. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvGreeting.text = getString(R.string.greeting, Session.name.substringBefore(' '))
        b.cardPending.setOnClickListener { findNavController().navigate(R.id.adminProductsFragment) }
        b.cardInactive.setOnClickListener { findNavController().navigate(R.id.adminUsersFragment) }
        b.btnSeed.setOnClickListener {
            requireContext().confirm(R.string.seed_title, getString(R.string.seed_message), R.string.load) { seed() }
        }
        collectFlow(Services.users.observeAll()) { users = it; render() }
        collectFlow(Services.products.observeAll()) { products = it; render() }
        collectFlow(Services.orders.observeAll()) { orders = it; render() }
    }

    /** Recalcula todos los indicadores a partir de las tres colecciones. */
    private fun render() {
        if (_b == null) return
        val sales = orders.filter { it.status.isSale }
        val now = Calendar.getInstance()
        val startOfDay = (now.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val startOfMonth = (startOfDay.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val today = sales.count { it.createdAt >= startOfDay.timeInMillis }
        val month = sales.filter { it.createdAt >= startOfMonth.timeInMillis }.sumOf { it.total }

        kpi(b.kpiUsers, R.drawable.ic_users, users.size.toString(), R.string.kpi_users)
        kpi(b.kpiProducts, R.drawable.ic_box, products.size.toString(), R.string.kpi_products)
        kpi(b.kpiOrders, R.drawable.ic_list, today.toString(), R.string.kpi_orders_today)
        kpi(b.kpiSales, R.drawable.ic_trend, "$ " + String.format(Locale("es", "CO"), "%.1f M", month / 1_000_000.0), R.string.kpi_sales_month)

        // Gráfico: ventas de los últimos 7 días (en millones de pesos), etiquetadas con la inicial del día.
        val letters = listOf("D", "L", "M", "X", "J", "V", "S")
        b.chart.values = (6 downTo 0).map { back ->
            val day = (startOfDay.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -back) }
            val end = day.timeInMillis + 86_400_000L
            val total = sales.filter { it.createdAt >= day.timeInMillis && it.createdAt < end }.sumOf { it.total }
            letters[day.get(Calendar.DAY_OF_WEEK) - 1] to (total / 1_000_000f)
        }.let { list -> if (list.all { it.second == 0f }) list.map { it.first to 0.0001f } else list }

        val pending = products.count { it.status == ProductStatus.PENDING }
        val inactive = users.count { !it.active }
        b.tvPending.text = resources.getQuantityString(R.plurals.pending_products, pending, pending)
        b.tvInactive.text = resources.getQuantityString(R.plurals.inactive_users, inactive, inactive)
    }

    /** Pinta una tarjeta de indicador (ícono, valor y etiqueta). */
    private fun kpi(s: ViewStatCardBinding, icon: Int, value: String, label: Int) {
        s.imgIcon.setImageResource(icon); s.tvValue.text = value; s.tvLabel.setText(label)
    }

    /** Crea productos de ejemplo ya aprobados para poder probar el catálogo sin tener vendedores. */
    private fun seed() = launchSafe(b.btnSeed) {
        val seller = Session.user ?: return@launchSafe
        val samples = listOf(
            Triple("Mochila wayúu", "Mochila tejida a mano por artesanas wayúu en Riohacha. Cada diseño es único.", 185_000L to "Tejidos"),
            Triple("Taza de cerámica", "Taza esmaltada y pintada a mano, apta para microondas. Capacidad de 300 ml.", 42_000L to "Cerámica"),
            Triple("Café de origen 500 g", "Café especial de altura, tostión media, notas de panela y cacao.", 28_500L to "Café"),
            Triple("Collar en plata", "Collar con cuentas de plata 950 y dije en latón dorado.", 96_000L to "Joyería"),
            Triple("Sombrero vueltiao", "Sombrero tradicional en caña flecha, 19 vueltas.", 240_000L to "Sombreros"),
            Triple("Tapete tejido", "Tapete de lana con patrón geométrico y tintes naturales.", 130_000L to "Tejidos"),
        )
        samples.forEach { (name, desc, priceCat) ->
            Services.products.save(Product(name = name, description = desc, price = priceCat.first, stock = 10, category = priceCat.second,
                sellerId = seller.id, sellerName = seller.name.ifBlank { "Tramas" }, status = ProductStatus.ACTIVE), null)
        }
        b.root.snack(getString(R.string.seed_done))
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
