package ru.gohasoft.wanderingtable.data.auth.repository

import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.network.auth.TokenPair
import ru.gohasoft.wanderingtable.core.network.auth.TokenRefresher
import ru.gohasoft.wanderingtable.data.auth.mapper.toTokenPair
import ru.gohasoft.wanderingtable.data.auth.remote.RemoteDataSource

internal class AuthApiTokenRefresher @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : TokenRefresher {

    override suspend fun refresh(refreshToken: String): TokenPair? = try {
        remoteDataSource.refresh(refreshToken).toTokenPair()
    } catch (unauthorized: NetworkException.Unauthorized) {
        // The token was already spent, revoked or expired — the session is over. Everything else
        // (no internet, 5xx) propagates, so a transient failure never looks like a logout.
        null
    }
}
