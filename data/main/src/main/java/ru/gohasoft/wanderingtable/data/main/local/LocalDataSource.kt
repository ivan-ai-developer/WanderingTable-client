package ru.gohasoft.wanderingtable.data.main.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import ru.gohasoft.wanderingtable.data.main.di.MainPreferences
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationSettingsDbo

/**
 * The device-local half of the main feature: the push feed and the push preferences. Neither has
 * a server endpoint, so this store is the source of truth for both.
 */
internal class LocalDataSource @Inject constructor(
    @param:MainPreferences private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {

    fun getNotifications(): Flow<List<NotificationDbo>> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS].decodeOr(emptyList())
    }

    suspend fun addNotification(notification: NotificationDbo) {
        updateNotifications { stored ->
            // Re-delivered pushes carry the id they had the first time; keep one row per id.
            listOf(notification) + stored.filterNot { it.id == notification.id }
        }
    }

    suspend fun markAsRead(notificationId: String) {
        updateNotifications { stored ->
            stored.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
    }

    suspend fun clearNotifications() {
        dataStore.edit { prefs -> prefs.remove(NOTIFICATIONS) }
    }

    fun getSettings(): Flow<NotificationSettingsDbo> = dataStore.data.map { prefs ->
        prefs[SETTINGS].decodeOr(NotificationSettingsDbo())
    }

    suspend fun saveSettings(settings: NotificationSettingsDbo) {
        dataStore.edit { prefs -> prefs[SETTINGS] = json.encodeToString(settings) }
    }

    private suspend fun updateNotifications(
        transform: (List<NotificationDbo>) -> List<NotificationDbo>,
    ) {
        dataStore.edit { prefs ->
            val stored = prefs[NOTIFICATIONS].decodeOr(emptyList<NotificationDbo>())
            prefs[NOTIFICATIONS] = json.encodeToString(transform(stored))
        }
    }

    /** A store written by an older build must not crash the feed — it falls back to the default. */
    private inline fun <reified T> String?.decodeOr(fallback: T): T =
        if (this == null) {
            fallback
        } else {
            try {
                json.decodeFromString<T>(this)
            } catch (malformed: SerializationException) {
                fallback
            }
        }

    private companion object {
        val NOTIFICATIONS = stringPreferencesKey("notifications")
        val SETTINGS = stringPreferencesKey("notification_settings")
    }
}
