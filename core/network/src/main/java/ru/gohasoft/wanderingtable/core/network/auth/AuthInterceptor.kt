package ru.gohasoft.wanderingtable.core.network.auth

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches `Authorization: Bearer <accessToken>` to every request on the authenticated client.
 * Requests made before a session exists go out unauthenticated and come back 401, which is the
 * same path an expired token takes — [TokenAuthenticator] decides what to do about it.
 */
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // OkHttp's interceptor API is blocking; token reads hit DataStore, which is suspend-only.
        val accessToken = runBlocking { tokenProvider.accessToken() } ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
                .build()
        )
    }

    internal companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
