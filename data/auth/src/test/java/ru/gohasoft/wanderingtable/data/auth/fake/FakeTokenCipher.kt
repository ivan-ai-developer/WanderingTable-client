package ru.gohasoft.wanderingtable.data.auth.fake

import ru.gohasoft.wanderingtable.data.auth.local.crypto.TokenCipher

/**
 * Identity "encryption" — the real implementation needs the Android Keystore, which is absent on
 * the JVM. Tests care about what is stored and cleared, not about the ciphertext.
 */
internal class FakeTokenCipher : TokenCipher {

    override fun encrypt(value: String): String = value

    override fun decrypt(value: String): String = value
}
