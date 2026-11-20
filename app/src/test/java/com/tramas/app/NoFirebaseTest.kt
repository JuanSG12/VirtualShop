package com.tramas.app

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tramas.app.data.Services
import com.tramas.app.ui.auth.LoginActivity
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

/** Con la Application REAL y sin google-services.json la app no debe cerrarse: debe abrir el login y avisar que falta configurar Firebase. */
@RunWith(AndroidJUnit4::class)
@Config(application = TramasApp::class)
class NoFirebaseTest {
    @Test fun loginSeAbreSinFirebaseConfigurado() {
        val activity = Robolectric.buildActivity(LoginActivity::class.java, Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)).setup().get()
        assertFalse("Firebase no debería estar listo sin google-services.json", Services.ready)
        assert(!activity.isFinishing)
    }
}
