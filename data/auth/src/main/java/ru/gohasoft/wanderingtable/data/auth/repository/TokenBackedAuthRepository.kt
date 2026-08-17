package ru.gohasoft.wanderingtable.data.auth.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.Unauthorized
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.data.auth.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.auth.mapper.toCachedUser
import ru.gohasoft.wanderingtable.data.auth.mapper.toTokenPair
import ru.gohasoft.wanderingtable.data.auth.mapper.toUser
import ru.gohasoft.wanderingtable.data.auth.remote.RemoteDataSource

/**
 * [AuthRepository] on top of the server's rotating token pair, persisted by [LocalDataSource]
 * and attached to requests by `:core:network`'s interceptor. Expiry is handled reactively — a 401
 * triggers a transparent refresh one layer down — so this class never inspects a token itself.
 */
internal class TokenBackedAuthRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : AuthRepository {

    override fun signUp(
        name: String,
        email: String,
        password: String
    ) = ResultFlow.onlineOnly {
        remoteDataSource.register(
            name = name,
            email = email,
            password = password
        ).toUser()
    }

    /**
     * Only a stored token pair may start [getSession]. Switching on every upstream emission would
     * also switch on the [Result.Loading] that `onlineOnly` emits before the request runs — at that
     * point nothing is saved yet, so the session would resolve to `Success(null)` — and it would
     * swallow a failed sign-in, reporting an empty session instead of the error.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun logIn(
        email: String,
        password: String
    ): Flow<Result<Session>> = ResultFlow.onlineOnly {
        val tokens = remoteDataSource.login(email, password)
        localDataSource.saveTokens(tokens.toTokenPair())
    }.flatMapLatest { result ->
        when (result) {
            is Result.Loading -> flowOf(Result.Loading())
            is Result.Error -> flowOf(Result.Error(result.error))
            is Result.Success -> getSession()
        }
    }

    override fun logOut(): Flow<Result<Unit>> =
        ResultFlow.onlineOnly {
            // Best effort: the endpoint is idempotent and losing the round trip only leaves a
            // refresh token alive server-side. Locally we always end up signed out.
            localDataSource.getTokens()?.let { tokens ->
                runCatching { remoteDataSource.logout(tokens.refreshToken) }
            }
            localDataSource.clear()
        }

    override fun requestPasswordReset(email: String): Flow<Result<Unit>> =
        ResultFlow.onlineOnly { remoteDataSource.forgotPassword(email) }

    override fun getSession(): Flow<Result<Session>> =
        ResultFlow.offlineFirst(
            query = ::getSessionFromCache,
            fetch = { remoteDataSource.getUser() },
            saveFetchResult = { fetched, _ -> localDataSource.saveUser(fetched.toCachedUser()) },
            // Tokens, not the cached profile, decide whether there is a session to refresh: right
            // after log in only the token pair exists, and that call is exactly the one that has to
            // populate the cache. Without tokens the request could only 401, so it is skipped.
            shouldFetch = { localDataSource.getTokens() != null },
        ).map(::keepCachedSessionOnTransientError)

    private fun getSessionFromCache(): Flow<Session?> {
        return localDataSource.getUser().map { cached ->
            cached?.let { Session(it.toUser()) }
        }
    }

    /**
     * Being offline must not sign the user out. A dead refresh token is different: the
     * authenticator wipes the store before the 401 surfaces, so [Result.Error.data] is already
     * `null` by then and the error is passed through to send the app back to the login screen.
     */
    private fun keepCachedSessionOnTransientError(result: Result<Session>): Result<Session> =
        if (result is Result.Error && result.error !is Unauthorized && result.data != null) {
            Result.Success(result.data)
        } else {
            result
        }
}
