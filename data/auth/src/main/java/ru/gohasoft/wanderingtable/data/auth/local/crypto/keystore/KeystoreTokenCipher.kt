package ru.gohasoft.wanderingtable.data.auth.local.crypto.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import ru.gohasoft.wanderingtable.data.auth.local.crypto.TokenCipher
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/**
 * AES/GCM encryption with a key that never leaves the Android Keystore, so the tokens on disk are
 * useless to anything that can read the app's DataStore file. The random IV is prefixed to the
 * ciphertext, and the whole thing is Base64'd because DataStore Preferences holds strings.
 */
internal class KeystoreTokenCipher @Inject constructor() : TokenCipher {

    private val lock = Any()

    override fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    override fun decrypt(value: String): String? = try {
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        if (bytes.size <= IV_SIZE_BYTES) {
            null
        } else {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE_BITS, bytes, 0, IV_SIZE_BYTES)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), spec)
            String(cipher.doFinal(bytes, IV_SIZE_BYTES, bytes.size - IV_SIZE_BYTES), Charsets.UTF_8)
        }
    } catch (security: GeneralSecurityException) {
        // The key was invalidated (screen lock changed, app data restored onto another device)
        // or the blob is corrupt. Either way the session is unrecoverable, not fatal.
        null
    } catch (malformed: IllegalArgumentException) {
        // Not valid Base64.
        null
    }

    private fun secretKey(): SecretKey = synchronized(lock) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey ?: generateKey()
    }

    private fun generateKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // Tokens must be readable while the device is locked (background refresh).
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "wandering_table_tokens"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_BYTES = 12
        const val TAG_SIZE_BITS = 128
    }
}