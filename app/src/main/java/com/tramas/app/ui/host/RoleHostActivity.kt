package com.tramas.app.ui.host

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MenuRes
import androidx.annotation.NavigationRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tramas.app.R
import com.tramas.app.data.Repo
import com.tramas.app.data.Role
import com.tramas.app.data.Session
import com.tramas.app.databinding.ActivityHostBinding
import com.tramas.app.databinding.NavHeaderBinding
import com.tramas.app.ui.auth.LoginActivity

/**
 * Contenedor común de los tres roles: Toolbar + Navigation Drawer + Bottom Navigation + NavHost.
 * Cada rol solo define su tema, su grafo de navegación y sus menús.
 */
abstract class RoleHostActivity : AppCompatActivity() {

    @get:StyleRes abstract val themeRes: Int
    @get:NavigationRes abstract val graphRes: Int
    @get:MenuRes abstract val bottomMenuRes: Int
    @get:MenuRes abstract val drawerMenuRes: Int
    abstract val topLevelIds: Set<Int>

    private lateinit var b: ActivityHostBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(themeRes)
        super.onCreate(savedInstanceState)
        b = ActivityHostBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)

        navController = (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
        navController.setGraph(graphRes)
        appBarConfig = AppBarConfiguration(topLevelIds, b.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)

        b.bottomNav.inflateMenu(bottomMenuRes)
        b.bottomNav.setupWithNavController(navController)
        b.navView.inflateMenu(drawerMenuRes)
        b.navView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.action_logout) confirmLogout() else NavigationUI.onNavDestinationSelected(item, navController)
            b.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        NavHeaderBinding.bind(b.navView.getHeaderView(0)).apply {
            tvAvatar.text = Session.name.first().toString()
            tvName.text = Session.name
            tvRole.text = "${Session.role.label} · ${Session.email}"
        }

        navController.addOnDestinationChangedListener { _, dest, _ ->
            b.bottomNav.isVisible = dest.id in topLevelIds
            b.navView.setCheckedItem(dest.id)
        }
        // Insignia con la cantidad de productos del carrito (solo existe en el rol Comprador)
        if (Session.role == Role.BUYER) {
            Repo.cart.observe(this) {
                val badge = b.bottomNav.getOrCreateBadge(R.id.cartFragment)
                badge.isVisible = Repo.cartCount() > 0
                badge.number = Repo.cartCount()
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    b.drawerLayout.isDrawerOpen(GravityCompat.START) -> b.drawerLayout.closeDrawer(GravityCompat.START)
                    !navController.popBackStack() -> finish()
                }
            }
        })
    }

    override fun onSupportNavigateUp() = navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.logout) { _, _ ->
                Repo.clearCart()
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
