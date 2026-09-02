package ru.gohasoft.wanderingtable.feature.main.fake

import java.time.Instant
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventType
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem

/** Fixture builders, so a test only spells out the fields it actually cares about. */
internal object TestGameEvents {

    /**
     * Anchored to the real clock: the ViewModels compare `startsAt` against `Instant.now()`,
     * so a hard-coded date would silently drift into the past and break "upcoming" tests.
     */
    val NOW: Instant = Instant.now()

    fun event(
        id: String = "e1",
        gameId: String = "g1",
        creatorId: String = "someone-else",
        type: GameEventType = GameEventType.REGULAR_GAME,
        status: GameEventStatus = GameEventStatus.PLANNED,
        participantsCount: Int = 1,
        maxParticipants: Int = 2,
        startsAt: Instant? = NOW.plusSeconds(3600),
        participants: List<String>? = null,
        description: String? = null,
    ): GameEvent = GameEvent(
        id = id,
        type = type,
        title = "Saturday game",
        description = description,
        gameId = gameId,
        creatorId = creatorId,
        status = status,
        minParticipants = 2,
        maxParticipants = maxParticipants,
        participantsCount = participantsCount,
        createdAt = NOW,
        startsAt = startsAt,
        durationMinutes = 90,
        participants = participants,
    )

    fun game(
        id: String = "g1",
        name: String = "Settlers of Catan",
        minPlayers: Int? = 2,
        maxPlayers: Int? = 4,
    ): Game = Game(
        id = id,
        name = name,
        description = null,
        minPlayers = minPlayers,
        maxPlayers = maxPlayers,
        resultType = GameResultType.POINTS,
    )

    fun news(id: String = "n1", ownerId: String = "author"): NewsItem = NewsItem(
        id = id,
        title = "Spring Team Championship",
        content = "Sign up in teams of two.",
        createdAt = NOW,
        ownerId = ownerId,
    )
}
