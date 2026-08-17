package ru.gohasoft.wanderingtable.core.network.auth

/**
 * Storage-agnostic access to the signed-in user's tokens. Implemented by the module that owns
 * auth persistence (`:data:auth`) so that this module never has to depend on it.
 */
interface TokenProvider {

    suspend fun accessToken(): String?

    suspend fun refreshToken(): String?

    suspend fun updateTokens(tokens: TokenPair)

    /** Wipes every trace of the session — called when the server rejects the refresh token. */
    suspend fun clearSession()
}
