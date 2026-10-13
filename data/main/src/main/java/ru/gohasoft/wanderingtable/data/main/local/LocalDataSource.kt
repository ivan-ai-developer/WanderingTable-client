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
import ru.gohasoft.wanderingtable.core.data.cache.CacheCleaner
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.data.main.di.MainPreferences
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameEventDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.NewsDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.ProfileDao
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationSettingsDbo
import ru.gohasoft.wanderingtable.data.main.mapper.toCachedProfileDbo
import ru.gohasoft.wanderingtable.data.main.mapper.toGameDbos
import ru.gohasoft.wanderingtable.data.main.mapper.toGameEventDbos
import ru.gohasoft.wanderingtable.data.main.mapper.toGameEventsFromCache
import ru.gohasoft.wanderingtable.data.main.mapper.toGameOrNull
import ru.gohasoft.wanderingtable.data.main.mapper.toGamesFromCache
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItem
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItemDbos
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItemsFromCache
import ru.gohasoft.wanderingtable.data.main.mapper.toUserProfile

/**
 * Everything the main feature keeps on disk, behind one door.
 *
 * Two stores sit behind it because they answer to different owners, not because they are different
 * concerns: Room holds the cached copies of server data (catalogue, schedule, feed, profile), and
 * a Preferences DataStore holds the push feed and the push preferences, neither of which has a
 * server endpoint at all — for those two this class is the source of truth, not a cache.
 *
 * Reads are `Flow`s straight off their store, so a write re-renders the screen without anyone
 * re-querying. Nothing here writes on its own: every `save*` is called by a repository that has
 * just fetched, or by an arriving push.
 */
internal class LocalDataSource @Inject constructor(
    private val gameDao: GameDao,
    private val gameEventDao: GameEventDao,
    private val newsDao: NewsDao,
    private val profileDao: ProfileDao,
    @param:MainPreferences private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : CacheCleaner {

    // --- Cached server data (Room) -------------------------------------------------------

    fun getGames(query: String?): Flow<List<Game>> =
        gameDao.observeGames(query.orEmpty()).map { it.toGamesFromCache() }

    fun getGame(gameId: String): Flow<Game?> =
        gameDao.observeGame(gameId).map { it?.toGameOrNull() }

    /** The catalogue is fetched whole, so storing it replaces it whole — deletions included. */
    suspend fun saveGames(games: List<Game>) = gameDao.replaceAll(games.toGameDbos())

    /** For a single game read back on its own: it must not wipe the rest of the catalogue. */
    suspend fun upsertGames(games: List<Game>) = gameDao.insertAll(games.toGameDbos())

    fun getEvents(scope: String): Flow<List<GameEvent>> =
        gameEventDao.observeEvents(scope).map { it.toGameEventsFromCache() }

    suspend fun saveEvents(scope: String, events: List<GameEvent>) =
        gameEventDao.replaceScope(scope, events.toGameEventDbos(scope))

    fun getNews(): Flow<List<NewsItem>> = newsDao.observeNews().map { it.toNewsItemsFromCache() }

    fun getNewsItem(newsId: String): Flow<NewsItem?> =
        newsDao.observeNewsItem(newsId).map { it?.toNewsItem() }

    suspend fun saveNews(news: List<NewsItem>) = newsDao.replaceAll(news.toNewsItemDbos())

    fun getProfile(): Flow<UserProfile?> = profileDao.observeProfile().map { it?.toUserProfile() }

    suspend fun saveProfile(profile: UserProfile) = profileDao.save(profile.toCachedProfileDbo())

    // --- Push feed and preferences (DataStore) -------------------------------------------

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

    // --- Logout ---------------------------------------------------------------------------

    /**
     * Drops what belonged to the account that is signing out: the cached server data and the push
     * feed, which is addressed to that user as much as their profile is.
     *
     * The notification *preferences* stay. They are a choice about this device — how loudly it
     * should behave — and re-asking for it on every sign-in would be the wrong kind of clean slate.
     */
    override suspend fun clear() {
        profileDao.deleteAll()
        gameEventDao.deleteAll()
        newsDao.deleteAll()
        gameDao.deleteAll()
        clearNotifications()
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

    companion object {
        private val NOTIFICATIONS = stringPreferencesKey("notifications")
        private val SETTINGS = stringPreferencesKey("notification_settings")

        /**
         * A cached list is keyed by the query that produced it, not just by entity: the club
         * schedule and the user's own plays overlap, and a refresh of one must not delete the
         * other's rows.
         */
        fun clubScope(status: String?): String = "club:${status ?: "all"}"

        fun userScope(userId: String): String = "user:$userId"
    }
}
