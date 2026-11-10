package com.tramas.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tramas.app.data.Repo
import com.tramas.app.data.Role
import com.tramas.app.data.Session
import com.tramas.app.ui.auth.ForgotPasswordActivity
import com.tramas.app.ui.auth.LoginActivity
import com.tramas.app.ui.auth.RegisterActivity
import com.tramas.app.ui.auth.SplashActivity
import com.tramas.app.ui.host.AdminActivity
import com.tramas.app.ui.host.BuyerActivity
import com.tramas.app.ui.host.SellerActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Infla todas las pantallas (detecta crashes de layouts/estilos) y guarda una captura de cada una. */
@RunWith(AndroidJUnit4::class)
class ScreensTest {
    private val out = File(System.getProperty("shots.dir") ?: "build/shots").apply { mkdirs() }

    private fun shot(name: String, act: Activity) {
        ShadowLooper.idleMainLooper()
        val v = act.window.decorView
        v.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(2200, View.MeasureSpec.EXACTLY))
        v.layout(0, 0, 1080, 2200)
        val bmp = Bitmap.createBitmap(1080, 2200, Bitmap.Config.ARGB_8888)
        v.draw(Canvas(bmp))
        File(out, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun <T : Activity> start(cls: Class<T>): T = Robolectric.buildActivity(cls, Intent(ApplicationProvider.getApplicationContext(), cls)).setup().get()

    @Test fun auth() {
        shot("00_splash", start(SplashActivity::class.java))
        shot("01_login", start(LoginActivity::class.java))
        shot("02_register", start(RegisterActivity::class.java))
        shot("03_forgot", start(ForgotPasswordActivity::class.java))
    }

    private fun tour(role: Role, cls: Class<out Activity>, prefix: String, steps: List<Pair<Int, android.os.Bundle?>>) {
        Session.start(role, "demo@tramas.co")
        val act = start(cls) as androidx.appcompat.app.AppCompatActivity
        val nav = (act.supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
        shot("${prefix}_00_start", act)
        steps.forEachIndexed { i, (dest, args) ->
            nav.navigate(dest, args)
            shot("${prefix}_%02d_%s".format(i + 1, act.resources.getResourceEntryName(dest)), act)
        }
    }

    @Test fun buyer() {
        Repo.addToCart(Repo.product(1)!!); Repo.addToCart(Repo.product(2)!!, 2); Repo.addToCart(Repo.product(3)!!)
        tour(Role.BUYER, BuyerActivity::class.java, "buyer", listOf(
            R.id.catalogFragment to null,
            R.id.productDetailFragment to android.os.Bundle().apply { putInt("productId", 1) },
            R.id.cartFragment to null, R.id.checkoutFragment to null, R.id.buyerOrdersFragment to null, R.id.buyerProfileFragment to null))
    }

    @Test fun seller() {
        tour(Role.SELLER, SellerActivity::class.java, "seller", listOf(
            R.id.productFormFragment to android.os.Bundle().apply { putInt("productId", 1) },
            R.id.sellerOrdersFragment to null, R.id.sellerProfileFragment to null))
    }

    @Test fun admin() {
        tour(Role.ADMIN, AdminActivity::class.java, "admin", listOf(
            R.id.adminUsersFragment to null,
            R.id.userFormFragment to android.os.Bundle().apply { putInt("userId", 2) },
            R.id.adminProductsFragment to null, R.id.adminSalesFragment to null))
    }
}
