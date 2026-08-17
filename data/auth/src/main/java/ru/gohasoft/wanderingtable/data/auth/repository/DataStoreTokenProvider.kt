package ru.gohasoft.wanderingtable.data.auth.repository

import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.network.auth.TokenPair
import ru.gohasoft.wanderingtable.core.network.auth.TokenProvider
import ru.gohasoft.wanderingtable.data.auth.local.LocalDataSource

internal class DataStoreTokenProvider @Inject constructor(
    private val localDataSource: LocalDataSource,
) : TokenProvider {

    override suspend fun accessToken(): String? = localDataSource.getTokens()?.accessToken

    override suspend fun refreshToken(): String? = localDataSource.getTokens()?.refreshToken

    override suspend fun updateTokens(tokens: TokenPair) = localDataSource.saveTokens(tokens)

    /**
     * Drops the cached user along with the tokens. `TokenBackedAuthRepository.getSession` reads
     * "cached user present" as "session present", so leaving the user behind would strand the app
     * on a session it can no longer authenticate.
     */
    override suspend fun clearSession() = localDataSource.clear()
}
