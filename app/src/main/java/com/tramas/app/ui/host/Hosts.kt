package com.tramas.app.ui.host

import android.app.Activity
import android.content.Intent
import com.tramas.app.R
import com.tramas.app.data.Role

/** Abre la interfaz que corresponde al rol del usuario y limpia la pila para que "atrás" no regrese al login. */
fun Activity.openHome(role: Role) {
    val target = when (role) {
        Role.BUYER -> BuyerActivity::class.java
        Role.SELLER -> SellerActivity::class.java
        Role.ADMIN -> AdminActivity::class.java
    }
    startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

/** Interfaz del COMPRADOR: tema verde, grafo nav_buyer y sus menús. */
class BuyerActivity : RoleHostActivity() {
    override val themeRes = R.style.Theme_Tramas_Buyer
    override val graphRes = R.navigation.nav_buyer
    override val bottomMenuRes = R.menu.bottom_buyer
    override val drawerMenuRes = R.menu.drawer_buyer
    override val topLevelIds = setOf(R.id.homeFragment, R.id.catalogFragment, R.id.cartFragment, R.id.buyerOrdersFragment, R.id.buyerProfileFragment)
}

/** Interfaz del VENDEDOR: tema terracota, grafo nav_seller y sus menús. */
class SellerActivity : RoleHostActivity() {
    override val themeRes = R.style.Theme_Tramas_Seller
    override val graphRes = R.navigation.nav_seller
    override val bottomMenuRes = R.menu.bottom_seller
    override val drawerMenuRes = R.menu.drawer_seller
    override val topLevelIds = setOf(R.id.sellerProductsFragment, R.id.sellerOrdersFragment, R.id.sellerProfileFragment)
}

/** Interfaz del ADMINISTRADOR: tema índigo, grafo nav_admin y sus menús. */
class AdminActivity : RoleHostActivity() {
    override val themeRes = R.style.Theme_Tramas_Admin
    override val graphRes = R.navigation.nav_admin
    override val bottomMenuRes = R.menu.bottom_admin
    override val drawerMenuRes = R.menu.drawer_admin
    override val topLevelIds = setOf(R.id.adminDashboardFragment, R.id.adminUsersFragment, R.id.adminProductsFragment, R.id.adminSalesFragment)
}
