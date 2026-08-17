package ru.gohasoft.wanderingtable.core.network.auth

import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.gohasoft.wanderingtable.core.network.auth.AuthInterceptor.Companion.AUTHORIZATION_HEADER
import ru.gohasoft.wanderingtable.core.network.auth.AuthInterceptor.Companion.BEARER_PREFIX

/**
 * Reacts to a 401 by exchanging the refresh token once and replaying the request.
 *
 * The server's refresh token is single-use: exchanging it invalidates it, and of several
 * concurrent exchanges of the same token exactly one succeeds. So the exchange is serialized on
 * [lock], and a caller that finds the stored access token already different from the one its
 * request sent simply replays with the newer token instead of spending the refresh token again.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider,
    // Lazy: the refresher is built on the plain Retrofit instance, which is constructed
    // independently of the authenticated one this authenticator belongs to.
    private val refresher: Provider<TokenRefresher>,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Already retried once with a fresh token and still 401 — stop, or OkHttp loops.
        if (response.responseCount >= MAX_ATTEMPTS) return null

        synchronized(lock) {
            val sentToken = response.request.header(AUTHORIZATION_HEADER)?.removePrefix(BEARER_PREFIX)
            val storedToken = runBlocking { tokenProvider.accessToken() }
            if (storedToken != null && storedToken != sentToken) {
                // Another thread refreshed while this request was in flight.
                return response.request.withBearer(storedToken)
            }

            val refreshToken = runBlocking { tokenProvider.refreshToken() } ?: return null

            val tokens = try {
                runBlocking { refresher.get().refresh(refreshToken) }
            } catch (io: IOException) {
                // Transient: let the caller see the network error rather than a bogus 401.
                throw io
            } catch (other: Exception) {
                throw IOException("token refresh failed", other)
            }

            if (tokens == null) {
                runBlocking { tokenProvider.clearSession() }
                return null
            }

            runBlocking { tokenProvider.updateTokens(tokens) }
            return response.request.withBearer(tokens.accessToken)
        }
    }

    private fun Request.withBearer(accessToken: String): Request = newBuilder()
        .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
        .build()

    private val Response.responseCount: Int
        get() {
            var count = 1
            var prior = priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }

    private companion object {
        const val MAX_ATTEMPTS = 2
    }
}
