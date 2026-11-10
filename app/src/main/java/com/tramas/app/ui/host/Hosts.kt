package com.tramas.app.ui.host

import com.tramas.app.R

class BuyerActivity : RoleHostActivity() {
    override val themeRes = R.style.Theme_Tramas_Buyer
    override val graphRes = R.navigation.nav_buyer
    override val bottomMenuRes = R.menu.bottom_buyer
    override val drawerMenuRes = R.menu.drawer_buyer
    override val topLevelIds = setOf(R.id.homeFragment, R.id.catalogFragment, R.id.cartFragment, R.id.buyerOrdersFragment, R.id.buyerProfileFragment)
}

class SellerActivity : RoleHostActivity() {
    override val themeRes = R.style.Theme_Tramas_Seller
    override val graphRes = R.navigation.nav_seller
    override val bottomMenuRes = R.menu.bottom_seller
    override val drawerMenuRes = R.menu.drawer_seller
    override val topLevelIds = setOf(R.id.sellerProductsFragment, R.id.sellerOrdersFragment, R.id.sellerProfileFragment)
}

class AdminActivity : RoleHostActivity() {
    override val themeRes = R.style.Theme_Tramas_Admin
    override val graphRes = R.navigation.nav_admin
    override val bottomMenuRes = R.menu.bottom_admin
    override val drawerMenuRes = R.menu.drawer_admin
    override val topLevelIds = setOf(R.id.adminDashboardFragment, R.id.adminUsersFragment, R.id.adminProductsFragment, R.id.adminSalesFragment)
}
