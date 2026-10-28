package com.example.raqeeb.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages encryption and decryption of sensitive connection credentials
 * (router passwords, ISP portal credentials) using Android KeyStore with AES-GCM.
 * Includes graceful JVM fallback for local unit test environments.
 */
class EncryptedCredentialsManager {

    private var keyStore: KeyStore? = null
    private var fallbackKey: SecretKey? = null

    init {
        try {
            val ks = KeyStore.getInstance(KEYSTORE_PROVIDER)
            ks.load(null)
            keyStore = ks
            getOrCreateSecretKey()
        } catch (_: Exception) {
            // AndroidKeyStore is not available in standard JVM / Robolectric unit test environments
            keyStore = null
            fallbackKey = SecretKeySpec("RaqeebNetFallbackKey32ByteAESKey!".toByteArray(Charsets.UTF_8).copyOf(32), "AES")
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val ks = keyStore ?: return fallbackKey!!
        if (!ks.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }
        return (ks.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            // Never log sensitive details or passwords
            return ""
        }
    }

    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) return ""
            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            val key = getOrCreateSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // Never log sensitive details
            return ""
        }
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "raqeeb_net_credential_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }
}
