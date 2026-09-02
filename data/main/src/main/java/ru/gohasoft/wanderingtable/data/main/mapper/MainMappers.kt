package ru.gohasoft.wanderingtable.data.main.mapper

import java.time.Instant
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException
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
import ru.gohasoft.wanderingtable.data.main.remote.dto.EventDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.FavoriteGameDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.GameDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.NoteDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.PageDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserProfileDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserStatsDto

/**
 * Enum names and timestamps are the only places the server can hand us something we cannot model.
 * Lists drop such rows, so one unrecognised event never blanks a whole screen; a single-object
 * read raises [SerializationException], which `withErrorHandling` reports as a parse failure.
 */

internal fun PageDto<GameDto>.toGames(): List<Game> = content.mapNotNull(GameDto::toGameOrNull)

internal fun GameDto.toGame(): Game = toGameOrNull() ?: unmappable("game", id)

internal fun GameDto.toGameOrNull(): Game? {
    val type = enumOrNull<GameResultType>(resultType) ?: return null
    return Game(
        id = id,
        name = name,
        description = description,
        minPlayers = minPlayers,
        maxPlayers = maxPlayers,
        resultType = type,
    )
}

internal fun PageDto<EventDto>.toGameEvents(): List<GameEvent> =
    content.mapNotNull(EventDto::toGameEventOrNull)

internal fun EventDto.toGameEvent(): GameEvent = toGameEventOrNull() ?: unmappable("event", id)

internal fun EventDto.toGameEventOrNull(): GameEvent? {
    val eventType = enumOrNull<GameEventType>(type) ?: return null
    val eventStatus = enumOrNull<GameEventStatus>(status) ?: return null
    val created = instantOrNull(createdAt) ?: return null
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
        createdAt = created,
        startsAt = startsAt?.let(::instantOrNull),
        durationMinutes = durationMinutes,
        participants = participants,
    )
}

internal fun PageDto<NoteDto>.toNewsItems(): List<NewsItem> = content.mapNotNull(NoteDto::toNewsItemOrNull)

internal fun NoteDto.toNewsItem(): NewsItem = toNewsItemOrNull() ?: unmappable("note", id)

internal fun NoteDto.toNewsItemOrNull(): NewsItem? {
    val created = instantOrNull(createdAt) ?: return null
    return NewsItem(
        id = id,
        title = title,
        content = content,
        createdAt = created,
        ownerId = ownerId,
    )
}

internal fun UserProfileDto.toUserProfile(): UserProfile = UserProfile(
    user = user.toUser(),
    stats = stats?.toUserStats(user.id) ?: emptyStats(user.id),
)

internal fun UserDto.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    roles = roles.toRoles(),
)

private fun UserStatsDto.toUserStats(fallbackUserId: String): UserStats = UserStats(
    userId = userId.ifEmpty { fallbackUserId },
    gamesPlayed = gamesPlayed,
    wins = wins,
    draws = draws,
    losses = losses,
    favoriteGames = favoriteGames.map(FavoriteGameDto::toFavoriteGame),
)

private fun FavoriteGameDto.toFavoriteGame(): FavoriteGame = FavoriteGame(
    gameId = gameId,
    name = name,
    playedCount = playedCount,
)

private fun emptyStats(userId: String): UserStats = UserStats(
    userId = userId,
    gamesPlayed = 0,
    wins = 0,
    draws = 0,
    losses = 0,
    favoriteGames = emptyList(),
)

/** Unknown role names are dropped: the server may add roles without changing the contract. */
private fun List<String>.toRoles(): List<Role> =
    mapNotNull { name -> Role.entries.firstOrNull { it.name == name } }

private inline fun <reified E : Enum<E>> enumOrNull(name: String): E? =
    enumValues<E>().firstOrNull { it.name == name }

private fun instantOrNull(value: String): Instant? =
    try {
        Instant.parse(value)
    } catch (malformed: DateTimeParseException) {
        null
    }

private fun unmappable(what: String, id: String): Nothing =
    throw SerializationException("Unsupported $what from server: $id")
