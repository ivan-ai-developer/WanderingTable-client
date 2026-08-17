package ru.gohasoft.wanderingtable.core.network.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Provider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * The server's refresh token is single-use, so the expensive failure mode here is spending it
 * twice: the second exchange is rejected and the user is silently signed out. These tests pin the
 * behaviour that prevents it.
 */
class TokenAuthenticatorTest {

    private val server = MockWebServer()
    private val tokenProvider = FakeTokenProvider()
    private val refreshCount = AtomicInteger()
    private var refreshResult: TokenPair? = TokenPair(NEW_ACCESS_TOKEN, "new-refresh")

    private val refresher = object : TokenRefresher {
        override suspend fun refresh(refreshToken: String): TokenPair? {
            refreshCount.incrementAndGet()
            // Widen the window in which another caller can observe the refresh in flight.
            Thread.sleep(REFRESH_DURATION_MILLIS)
            return refreshResult
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenProvider))
        .authenticator(TokenAuthenticator(tokenProvider, Provider { refresher }))
        .build()

    @BeforeEach
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val authorized = request.getHeader("Authorization") == "Bearer $NEW_ACCESS_TOKEN"
                return MockResponse().setResponseCode(if (authorized) 200 else 401)
            }
        }
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `concurrent 401s spend the refresh token exactly once`() {
        tokenProvider.tokens = TokenPair(STALE_ACCESS_TOKEN, "refresh-1")

        // Start every call before joining any, so they genuinely overlap.
        val codes = (1..CONCURRENT_CALLS).map { call() }.map { await -> await() }

        assertThat(refreshCount.get()).isEqualTo(1)
        codes.forEach { code -> assertThat(code).isEqualTo(200) }
        assertThat(tokenProvider.tokens?.accessToken).isEqualTo(NEW_ACCESS_TOKEN)
    }

    @Test
    fun `a refused refresh clears the session and gives up`() {
        tokenProvider.tokens = TokenPair(STALE_ACCESS_TOKEN, "refresh-1")
        refreshResult = null

        val code = call()()

        assertThat(code).isEqualTo(401)
        assertThat(tokenProvider.tokens).isNull()
    }

    @Test
    fun `no refresh token means no refresh attempt`() {
        tokenProvider.tokens = null

        val code = call()()

        assertThat(code).isEqualTo(401)
        assertThat(refreshCount.get()).isEqualTo(0)
    }

    /**
     * Fires a request on its own thread and returns a function that joins it and yields the status
     * code. The authenticator blocks, so reproducing the race needs real threads.
     */
    private fun call(): () -> Int {
        val request = Request.Builder().url(server.url("/users/me")).build()
        val code = AtomicInteger(-1)
        val thread = Thread { client.newCall(request).execute().use { code.set(it.code) } }
        thread.start()
        return {
            thread.join()
            code.get()
        }
    }

    private class FakeTokenProvider : TokenProvider {

        @Volatile
        var tokens: TokenPair? = null

        override suspend fun accessToken(): String? = tokens?.accessToken

        override suspend fun refreshToken(): String? = tokens?.refreshToken

        override suspend fun updateTokens(tokens: TokenPair) {
            this.tokens = tokens
        }

        override suspend fun clearSession() {
            tokens = null
        }
    }

    private companion object {
        const val STALE_ACCESS_TOKEN = "stale-access"
        const val NEW_ACCESS_TOKEN = "new-access"
        const val CONCURRENT_CALLS = 4
        const val REFRESH_DURATION_MILLIS = 100L
    }
}
