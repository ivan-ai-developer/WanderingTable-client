package ru.gohasoft.wanderingtable.feature.main.fake

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository

internal class FakeGameEventRepository : GameEventRepository {

    var events: List<GameEvent> = emptyList()
    var userGames: List<GameEvent> = emptyList()
    var getEventsResult: Result<List<GameEvent>>? = null
    var getEventResult: Result<GameEvent>? = null
    var getUserGamesResult: Result<List<GameEvent>>? = null
    var joinResult: Result<GameEvent> = Result.Success(null)
    var leaveResult: Result<Unit> = Result.Success(Unit)
    var cancelResult: Result<GameEvent> = Result.Success(null)

    var joinCallCount = 0
    var leaveCallCount = 0
    var cancelCallCount = 0
    var createdRequests = mutableListOf<CreatedRegularGame>()

    override fun getEvents(status: GameEventStatus?): Flow<Result<List<GameEvent>>> =
        flowOf(getEventsResult ?: Result.Success(events))

    override fun getEvent(eventId: String): Flow<Result<GameEvent>> =
        flowOf(getEventResult ?: Result.Success(events.firstOrNull { it.id == eventId }))

    override fun getUserGames(userId: String): Flow<Result<List<GameEvent>>> =
        flowOf(getUserGamesResult ?: Result.Success(userGames))

    override fun createRegularGame(
        gameId: String,
        title: String,
        description: String?,
        startsAt: Instant,
        durationMinutes: Int,
        minParticipants: Int,
        maxParticipants: Int,
    ): Flow<Result<GameEvent>> {
        createdRequests += CreatedRegularGame(
            gameId = gameId,
            title = title,
            description = description,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            minParticipants = minParticipants,
            maxParticipants = maxParticipants,
        )
        return flowOf(Result.Success(null))
    }

    override fun join(eventId: String): Flow<Result<GameEvent>> {
        joinCallCount++
        return flowOf(joinResult)
    }

    override fun leave(eventId: String): Flow<Result<Unit>> {
        leaveCallCount++
        return flowOf(leaveResult)
    }

    override fun cancel(eventId: String): Flow<Result<GameEvent>> {
        cancelCallCount++
        return flowOf(cancelResult)
    }

    /** Captures exactly what the Create Request screen posted, so tests can assert the payload. */
    internal data class CreatedRegularGame(
        val gameId: String,
        val title: String,
        val description: String?,
        val startsAt: Instant,
        val durationMinutes: Int,
        val minParticipants: Int,
        val maxParticipants: Int,
    )
}
