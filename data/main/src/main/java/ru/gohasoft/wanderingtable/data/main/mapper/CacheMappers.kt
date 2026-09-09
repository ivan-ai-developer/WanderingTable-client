package ru.gohasoft.wanderingtable.data.main.mapper

import java.time.Instant
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventType
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.model.user.FavoriteGame
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.core.domain.model.user.UserStats
import ru.gohasoft.wanderingtable.data.main.local.dbo.CachedProfileDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.FavoriteGameDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.GameDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.GameEventDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.NewsItemDbo

/**
 * Domain ↔ cache. Only values the network mappers already accepted are ever written, so a stored
 * enum name can still fail to resolve after a downgrade — such a row is dropped on read, exactly
 * as an unmappable server row is, rather than blanking the whole list.
 */

internal fun List<Game>.toGameDbos(): List<GameDbo> = map(Game::toGameDbo)

internal fun Game.toGameDbo(): GameDbo = GameDbo(
    id = id,
    name = name,
    description = description,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    resultType = resultType.name,
)

internal fun List<GameDbo>.toGamesFromCache(): List<Game> = mapNotNull(GameDbo::toGameOrNull)

internal fun GameDbo.toGameOrNull(): Game? {
    val type = cachedEnumOrNull<GameResultType>(resultType) ?: return null
    return Game(
        id = id,
        name = name,
        description = description,
        minPlayers = minPlayers,
        maxPlayers = maxPlayers,
        resultType = type,
    )
}

internal fun List<GameEvent>.toGameEventDbos(scope: String): List<GameEventDbo> =
    mapIndexed { index, event -> event.toGameEventDbo(scope, index) }

internal fun GameEvent.toGameEventDbo(scope: String, position: Int): GameEventDbo = GameEventDbo(
    scope = scope,
    id = id,
    type = type.name,
    title = title,
    description = description,
    gameId = gameId,
    creatorId = creatorId,
    status = status.name,
    minParticipants = minParticipants,
    maxParticipants = maxParticipants,
    participantsCount = participantsCount,
    createdAt = createdAt.toEpochMilli(),
    startsAt = startsAt?.toEpochMilli(),
    durationMinutes = durationMinutes,
    position = position,
)

internal fun List<GameEventDbo>.toGameEventsFromCache(): List<GameEvent> =
    mapNotNull(GameEventDbo::toGameEventOrNull)

internal fun GameEventDbo.toGameEventOrNull(): GameEvent? {
    val eventType = cachedEnumOrNull<GameEventType>(type) ?: return null
    val eventStatus = cachedEnumOrNull<GameEventStatus>(status) ?: return null
    return GameEvent(
        id = id,
        type = eventType,
        title = title,
        description = description,
        gameId = gameId,
        creatorId = creatorId,
        status = eventStatus,
        minParticipants = minParticipants,
        maxParticipants = maxParticipants,
        participantsCount = participantsCount,
        createdAt = Instant.ofEpochMilli(createdAt),
        startsAt = startsAt?.let(Instant::ofEpochMilli),
        durationMinutes = durationMinutes,
        // List responses omit the roster, so nothing was ever stored to hand back here.
        participants = null,
    )
}

internal fun List<NewsItem>.toNewsItemDbos(): List<NewsItemDbo> =
    mapIndexed { index, item -> item.toNewsItemDbo(index) }

internal fun NewsItem.toNewsItemDbo(position: Int): NewsItemDbo = NewsItemDbo(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt.toEpochMilli(),
    ownerId = ownerId,
    position = position,
)

internal fun List<NewsItemDbo>.toNewsItemsFromCache(): List<NewsItem> = map(NewsItemDbo::toNewsItem)

internal fun NewsItemDbo.toNewsItem(): NewsItem = NewsItem(
    id = id,
    title = title,
    content = content,
    createdAt = Instant.ofEpochMilli(createdAt),
    ownerId = ownerId,
)

internal fun UserProfile.toCachedProfileDbo(): CachedProfileDbo = CachedProfileDbo(
    userId = user.id,
    name = user.name,
    email = user.email,
    avatarUrl = user.avatarUrl,
    roles = user.roles.map(Role::name),
    gamesPlayed = stats.gamesPlayed,
    wins = stats.wins,
    draws = stats.draws,
    losses = stats.losses,
    favoriteGames = stats.favoriteGames.map { favorite ->
        FavoriteGameDbo(
            gameId = favorite.gameId,
            name = favorite.name,
            playedCount = favorite.playedCount,
        )
    },
)

internal fun CachedProfileDbo.toUserProfile(): UserProfile = UserProfile(
    user = User(
        id = userId,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        roles = roles.mapNotNull { name -> cachedEnumOrNull<Role>(name) },
    ),
    stats = UserStats(
        userId = userId,
        gamesPlayed = gamesPlayed,
        wins = wins,
        draws = draws,
        losses = losses,
        favoriteGames = favoriteGames.map { favorite ->
            FavoriteGame(
                gameId = favorite.gameId,
                name = favorite.name,
                playedCount = favorite.playedCount,
            )
        },
    ),
)

private inline fun <reified E : Enum<E>> cachedEnumOrNull(name: String): E? =
    enumValues<E>().firstOrNull { it.name == name }
