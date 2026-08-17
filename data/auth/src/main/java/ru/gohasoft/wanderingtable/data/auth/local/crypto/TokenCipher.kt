package ru.gohasoft.wanderingtable.data.auth.local.crypto

/**
 * Symmetric encryption for the token values written to disk. An interface so that repository and
 * data-source tests can run on the JVM, where there is no Android Keystore.
 */
internal interface TokenCipher {

    fun encrypt(value: String): String

    /** Returns `null` when the stored value cannot be recovered — treat that as "no session". */
    fun decrypt(value: String): String?
}
