package com.tramas.app.ui.common

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tramas.app.R
import com.tramas.app.security.BiometricVault
import javax.crypto.Cipher

/** Ayudante del ingreso con huella: activar (guardar la contraseña protegida) e ingresar (recuperarla con la huella). */
object BiometricHelper {

    /** True si el dispositivo tiene lector y al menos una huella registrada (clase fuerte). */
    fun isAvailable(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Activa el ingreso con huella: pide la huella y, con la llave que esta desbloquea, guarda cifrada la contraseña.
     * @param onDone true si quedó activado.
     */
    fun enroll(activity: FragmentActivity, vault: BiometricVault, email: String, password: String, onDone: (Boolean, String?) -> Unit) {
        val cipher = try { vault.encryptCipher() } catch (e: Exception) { onDone(false, e.message); return }
        prompt(activity, cipher, activity.getString(R.string.biometric_enable_title), onSuccess = { c ->
            vault.store(email, password, c)
            onDone(true, null)
        }, onError = { onDone(false, it) })
    }

    /**
     * Ingresa con huella: pide la huella, descifra la contraseña guardada y se la entrega a [onPassword].
     * Si el usuario cambió las huellas registradas del teléfono, la llave se invalida y hay que volver a activar.
     */
    fun login(activity: FragmentActivity, vault: BiometricVault, onPassword: (email: String, password: String) -> Unit, onError: (String) -> Unit) {
        val cipher = try {
            vault.decryptCipher()
        } catch (e: KeyPermanentlyInvalidatedException) {
            vault.clear(); onError(activity.getString(R.string.biometric_invalidated)); return
        } catch (e: Exception) {
            onError(e.message ?: activity.getString(R.string.biometric_failed)); return
        }
        prompt(activity, cipher, activity.getString(R.string.biometric_title), onSuccess = { c ->
            onPassword(vault.savedEmail().orEmpty(), vault.read(c))
        }, onError = onError)
    }

    /** Muestra el diálogo del sistema para la huella, ligado al [cipher] (CryptoObject). */
    private fun prompt(activity: FragmentActivity, cipher: Cipher, title: String, onSuccess: (Cipher) -> Unit, onError: (String) -> Unit) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            /** La huella fue reconocida: entrega el Cipher desbloqueado al llamador. */
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val c = result.cryptoObject?.cipher
                if (c == null) onError(activity.getString(R.string.biometric_failed)) else onSuccess(c)
            }
            /** Error o cancelación del diálogo de huella; el cancelar del usuario no se informa. */
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // El botón "Usar contraseña" y el cancelar del usuario no son errores que haya que mostrar.
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) onError("") else onError(errString.toString())
            }
        }
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(activity.getString(R.string.biometric_subtitle))
            .setNegativeButtonText(activity.getString(R.string.use_password))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback).authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }
}
