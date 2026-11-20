package com.tramas.app.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Bóveda para el ingreso con huella.
 *
 * Idea: tras un inicio de sesión con contraseña, el usuario puede "activar la huella". La contraseña se cifra con una
 * llave AES-GCM guardada en el Android Keystore que SOLO se puede usar tras autenticarse con la huella
 * (setUserAuthenticationRequired). Al iniciar con huella se descifra y se usa para entrar con Firebase Authentication.
 * La contraseña nunca se guarda en texto plano y la huella nunca sale del sistema operativo.
 */
class BiometricVault(context: Context) {

    private val prefs = context.getSharedPreferences("biometric_vault", Context.MODE_PRIVATE)

    /** True si hay credenciales guardadas para ingresar con huella. */
    fun isEnabled(): Boolean = prefs.contains(KEY_DATA) && prefs.contains(KEY_EMAIL)

    /** Correo asociado a las credenciales guardadas. */
    fun savedEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /** Cipher listo para CIFRAR; se entrega al BiometricPrompt como CryptoObject. */
    fun encryptCipher(): Cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }

    /**
     * Cipher listo para DESCIFRAR con el IV guardado.
     * Lanza KeyPermanentlyInvalidatedException si el usuario cambió sus huellas registradas: en ese caso hay que volver a activar.
     */
    fun decryptCipher(): Cipher {
        val iv = Base64.decode(prefs.getString(KEY_IV, "")!!, Base64.NO_WRAP)
        return Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv)) }
    }

    /** Cifra y guarda la contraseña usando el cipher ya autorizado por la huella. */
    fun store(email: String, password: String, cipher: Cipher) {
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_DATA, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    /** Descifra la contraseña guardada con el cipher ya autorizado por la huella. */
    fun read(cipher: Cipher): String {
        val data = Base64.decode(prefs.getString(KEY_DATA, "")!!, Base64.NO_WRAP)
        return String(cipher.doFinal(data), Charsets.UTF_8)
    }

    /** Borra las credenciales y la llave (desactivar huella o cerrar sesión definitivamente). */
    fun clear() {
        prefs.edit().clear().apply()
        runCatching { KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(ALIAS) }
    }

    /** Obtiene la llave del Keystore o la crea la primera vez. */
    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .apply {
                // Exige huella en CADA uso de la llave (timeout 0 + CryptoObject).
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            }
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply { init(spec) }.generateKey()
    }

    private companion object {
        const val ALIAS = "tramas_biometric_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_EMAIL = "email"
        const val KEY_DATA = "data"
        const val KEY_IV = "iv"
    }
}
