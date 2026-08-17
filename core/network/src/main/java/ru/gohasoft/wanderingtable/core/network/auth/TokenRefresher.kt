package ru.gohasoft.wanderingtable.core.network.auth

/**
 * Performs the `POST /auth/refresh` exchange. Implemented in `:data:auth` on top of the
 * [ru.gohasoft.wanderingtable.core.network.di.PlainClient] Retrofit instance, so refreshing can
 * never re-enter [TokenAuthenticator].
 */
interface TokenRefresher {

    /**
     * Returns the new pair, or `null` when the server refused the token (401) — meaning the
     * session is definitively over. Transient failures (no internet, 5xx) are thrown instead,
     * so a network hiccup is never mistaken for a logout.
     */
    suspend fun refresh(refreshToken: String): TokenPair?
}
