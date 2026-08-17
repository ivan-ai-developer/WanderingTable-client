package ru.gohasoft.wanderingtable.data.auth.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.network.auth.TokenPair
import ru.gohasoft.wanderingtable.data.auth.fake.FakeAuthApi
import ru.gohasoft.wanderingtable.data.auth.fake.FakePreferencesDataStore
import ru.gohasoft.wanderingtable.data.auth.fake.FakeSessionApi
import ru.gohasoft.wanderingtable.data.auth.fake.FakeTokenCipher
import ru.gohasoft.wanderingtable.data.auth.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.auth.local.dbo.CachedUserDbo
import ru.gohasoft.wanderingtable.data.auth.remote.RemoteDataSource
import ru.gohasoft.wanderingtable.data.auth.remote.dto.AuthTokensResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto

class TokenBackedAuthRepositoryTest {

    private val authApi = FakeAuthApi()
    private val sessionApi = FakeSessionApi()
    private val localDataSource = LocalDataSource(
        dataStore = FakePreferencesDataStore(),
        cipher = FakeTokenCipher(),
        json = Json { ignoreUnknownKeys = true },
    )
    private val repository = TokenBackedAuthRepository(
        remoteDataSource = RemoteDataSource(authApi, sessionApi),
        localDataSource = localDataSource,
    )

    @Test
    fun `log in stores the token pair and the profile from users me`() = runTest {
        authApi.loginResult = AuthTokensResponseDto("access-1", "refresh-1")
        sessionApi.meResult = UserDto(
            id = "user-7",
            name = "Alice",
            email = "alice@example.com",
            roles = listOf("PLAYER", "CLUB_MANAGER"),
        )

        val result = repository.logIn("alice@example.com", "Password1").awaitFinalResult()

        val user = (result as Result.Success).data?.user
        assertThat(user?.id).isEqualTo("user-7")
        assertThat(user?.name).isEqualTo("Alice")
        assertThat(user?.roles).isEqualTo(listOf(Role.PLAYER, Role.CLUB_MANAGER))
        assertThat(localDataSource.getTokens()).isEqualTo(TokenPair("access-1", "refresh-1"))
    }

    @Test
    fun `unknown server roles are dropped rather than failing the login`() = runTest {
        sessionApi.meResult = sessionApi.meResult.copy(roles = listOf("PLAYER", "TIME_TRAVELLER"))

        val result = repository.logIn("alice@example.com", "Password1").awaitFinalResult()

        assertThat((result as Result.Success).data?.user?.roles).isEqualTo(listOf(Role.PLAYER))
    }

    @Test
    fun `sign up uses the registration response instead of a second users me call`() = runTest {
        val result = repository.signUp("Bob", "bob@example.com", "Password1").awaitFinalResult()

        val user = (result as Result.Success).data
        assertThat(user?.name).isEqualTo("Bob")
        assertThat(user?.email).isEqualTo("bob@example.com")
        assertThat(authApi.registerCallCount).isEqualTo(1)
        assertThat(sessionApi.meCallCount).isEqualTo(0)
    }

    @Test
    fun `log out revokes the refresh token server-side and clears local state`() = runTest {
        localDataSource.saveTokens(TokenPair("access-1", "refresh-1"))
        localDataSource.saveUser(cachedUser())

        repository.logOut().awaitFinalResult()

        assertThat(authApi.loggedOutTokens).containsExactly("refresh-1")
        assertThat(localDataSource.getTokens()).isNull()
        assertThat(localDataSource.getUser().first()).isNull()
    }

    @Test
    fun `log out clears local state even when the server call fails`() = runTest {
        localDataSource.saveTokens(TokenPair("access-1", "refresh-1"))
        localDataSource.saveUser(cachedUser())
        authApi.logoutError = NetworkException.NoInternet()

        val result = repository.logOut().awaitFinalResult()

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat(localDataSource.getTokens()).isNull()
        assertThat(localDataSource.getUser().first()).isNull()
    }

    @Test
    fun `no cached user resolves to no session without touching the network`() = runTest {
        val result = repository.getSession().awaitFinalResult()

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat((result as Result.Success).data).isNull()
        assertThat(sessionApi.meCallCount).isEqualTo(0)
    }

    @Test
    fun `cached session is refreshed from users me`() = runTest {
        localDataSource.saveTokens(TokenPair("access-1", "refresh-1"))
        localDataSource.saveUser(cachedUser(name = "Stale name"))
        sessionApi.meResult = sessionApi.meResult.copy(name = "Fresh name")

        val result = repository.getSession().awaitFinalResult()

        assertThat(sessionApi.meCallCount).isEqualTo(1)
        assertThat((result as Result.Success).data?.user?.name).isEqualTo("Fresh name")
        assertThat(localDataSource.getUser().first()?.name).isEqualTo("Fresh name")
    }

    @Test
    fun `being offline keeps the cached session instead of signing the user out`() = runTest {
        localDataSource.saveTokens(TokenPair("access-1", "refresh-1"))
        localDataSource.saveUser(cachedUser(name = "Cached name"))
        sessionApi.meError = NetworkException.NoInternet()

        val result = repository.getSession().awaitFinalResult()

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat((result as Result.Success).data?.user?.name).isEqualTo("Cached name")
    }

    @Test
    fun `a rejected token surfaces as an error so the app returns to login`() = runTest {
        localDataSource.saveTokens(TokenPair("access-1", "refresh-1"))
        localDataSource.saveUser(cachedUser())
        // In production the authenticator has already wiped the store by the time this surfaces;
        // what matters here is that Unauthorized is never softened into a Success.
        sessionApi.meError = NetworkException.Unauthorized()

        val result = repository.getSession().awaitFinalResult()

        assertThat(result).isInstanceOf(Result.Error::class)
        assertThat((result as Result.Error).error).isInstanceOf(NetworkException.Unauthorized::class)
    }

    private fun cachedUser(name: String = "Alice") = CachedUserDbo(
        id = "user-1",
        name = name,
        email = "alice@example.com",
        roles = listOf("PLAYER"),
    )

    private suspend fun <T> Flow<Result<T>>.awaitFinalResult(): Result<T> {
        var last: Result<T>? = null
        test {
            while (true) {
                val item = awaitItem()
                if (item !is Result.Loading) {
                    last = item
                    break
                }
            }
            cancelAndIgnoreRemainingEvents()
        }
        return requireNotNull(last)
    }
}
