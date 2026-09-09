package ru.gohasoft.wanderingtable.data.main.local

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameEventDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.NewsDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.ProfileDao
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
 * The offline copy of what Home, Games and Profile read, in domain terms.
 *
 * Reads are `Flow`s straight off Room, so a refresh that writes new rows re-renders the screen
 * without anyone re-querying. Nothing here writes on its own — every `save*` is called by a
 * repository that has just fetched.
 */
internal class CacheDataSource @Inject constructor(
    private val gameDao: GameDao,
    private val gameEventDao: GameEventDao,
    private val newsDao: NewsDao,
    private val profileDao: ProfileDao,
) {

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

    fun getProfile(): Flow<UserProfile?> =
        profileDao.observeProfile().map { it?.toUserProfile() }

    suspend fun saveProfile(profile: UserProfile) = profileDao.save(profile.toCachedProfileDbo())

    /** Called on logout: the next account on this device must not see any of it. */
    suspend fun clear() {
        profileDao.deleteAll()
        gameEventDao.deleteAll()
        newsDao.deleteAll()
        gameDao.deleteAll()
    }

    companion object {
        /**
         * A cached list is keyed by the query that produced it, not just by entity: the club
         * schedule and the user's own plays overlap, and a refresh of one must not delete the
         * other's rows.
         */
        fun clubScope(status: String?): String = "club:${status ?: "all"}"

        fun userScope(userId: String): String = "user:$userId"
    }
}
