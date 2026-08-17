package ru.gohasoft.wanderingtable.data.auth.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.network.auth.TokenPair
import ru.gohasoft.wanderingtable.data.auth.fake.FakePreferencesDataStore
import ru.gohasoft.wanderingtable.data.auth.fake.FakeTokenCipher
import ru.gohasoft.wanderingtable.data.auth.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.auth.local.dbo.CachedUserDbo

class DataStoreTokenProviderTest {

    private val localDataSource = LocalDataSource(
        dataStore = FakePreferencesDataStore(),
        cipher = FakeTokenCipher(),
        json = Json { ignoreUnknownKeys = true },
    )
    private val provider = DataStoreTokenProvider(localDataSource)

    @Test
    fun `updated tokens are readable back`() = runTest {
        provider.updateTokens(TokenPair("access-2", "refresh-2"))

        assertThat(provider.accessToken()).isEqualTo("access-2")
        assertThat(provider.refreshToken()).isEqualTo("refresh-2")
    }

    @Test
    fun `clearing the session drops the cached user too`() = runTest {
        provider.updateTokens(TokenPair("access-1", "refresh-1"))
        localDataSource.saveUser(CachedUserDbo("user-1", "Alice", "alice@example.com"))

        provider.clearSession()

        assertThat(provider.accessToken()).isNull()
        // TokenBackedAuthRepository.getSession treats a cached user as proof of a session, so
        // leaving one behind would strand the app on credentials it can no longer refresh.
        assertThat(localDataSource.getUser().first()).isNull()
    }
}
