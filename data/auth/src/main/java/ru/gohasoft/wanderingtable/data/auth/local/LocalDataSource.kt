package ru.gohasoft.wanderingtable.data.auth.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import ru.gohasoft.wanderingtable.core.network.auth.TokenPair
import ru.gohasoft.wanderingtable.data.auth.local.crypto.TokenCipher
import ru.gohasoft.wanderingtable.data.auth.local.dbo.CachedUserDbo

/**
 * Everything the session needs on disk: the token pair (encrypted via [TokenCipher]) and a cached
 * copy of the user profile. Tokens and cached user are written and cleared together, which is what
 * lets the repository treat "a cached user exists" as "a session exists".
 */
internal class LocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val cipher: TokenCipher,
    private val json: Json,
) {

    suspend fun getTokens(): TokenPair? {
        val prefs = dataStore.data.first()
        val accessToken = prefs[ACCESS_TOKEN]?.let(cipher::decrypt) ?: return null
        val refreshToken = prefs[REFRESH_TOKEN]?.let(cipher::decrypt) ?: return null
        return TokenPair(accessToken, refreshToken)
    }

    suspend fun saveTokens(tokens: TokenPair) {
        val accessToken = cipher.encrypt(tokens.accessToken)
        val refreshToken = cipher.encrypt(tokens.refreshToken)
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    fun getUser(): Flow<CachedUserDbo?> = dataStore.data.map { prefs ->
        prefs[USER]?.let { stored ->
            try {
                json.decodeFromString<CachedUserDbo>(stored)
            } catch (malformed: SerializationException) {
                null
            }
        }
    }

    suspend fun saveUser(user: CachedUserDbo) {
        val encoded = json.encodeToString(user)
        dataStore.edit { prefs -> prefs[USER] = encoded }
    }

    suspend fun clear() {
        dataStore.edit { prefs -> prefs.clear() }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER = stringPreferencesKey("user")
    }
}
