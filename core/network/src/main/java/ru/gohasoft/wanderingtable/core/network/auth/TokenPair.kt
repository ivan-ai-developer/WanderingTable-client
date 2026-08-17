package ru.gohasoft.wanderingtable.core.network.auth

/** The server's rotating credential pair. Both tokens change on every successful refresh. */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)
