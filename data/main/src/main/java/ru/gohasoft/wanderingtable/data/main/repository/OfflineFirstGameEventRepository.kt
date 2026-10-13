package ru.gohasoft.wanderingtable.data.main.repository

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.data.main.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.main.mapper.toGameEvent
import ru.gohasoft.wanderingtable.data.main.mapper.toGameEvents
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateRegularGameRequestDto

/**
 * The two schedule lists — the club's and the user's own — are cached under separate scopes,
 * because the same play belongs to both and each list is refreshed on its own.
 *
 * Only the lists are cached. A single event is not: list responses omit `participants`, so a
 * cached row could only ever claim an empty roster, and the detail screen is the one place that
 * needs the real one.
 */
internal class OfflineFirstGameEventRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : GameEventRepository {

    override fun getEvents(status: GameEventStatus?): Flow<Result<List<GameEvent>>> {
        val scope = LocalDataSource.clubScope(status?.name)
        return ResultFlow.offlineFirst(
            query = { localDataSource.getEvents(scope) },
            fetch = { remoteDataSource.getEvents(status?.name).toGameEvents() },
            saveFetchResult = { fetched, _ -> localDataSource.saveEvents(scope, fetched) },
        )
    }

    override fun getEvent(eventId: String): Flow<Result<GameEvent>> =
        ResultFlow.onlineOnly { remoteDataSource.getEvent(eventId).toGameEvent() }

    override fun getUserGames(userId: String): Flow<Result<List<GameEvent>>> {
        val scope = LocalDataSource.userScope(userId)
        return ResultFlow.offlineFirst(
            query = { localDataSource.getEvents(scope) },
            fetch = { remoteDataSource.getUserGames(userId).toGameEvents() },
            saveFetchResult = { fetched, _ -> localDataSource.saveEvents(scope, fetched) },
        )
    }

    override fun createRegularGame(
        gameId: String,
        title: String,
        description: String?,
        startsAt: Instant,
        durationMinutes: Int,
        minParticipants: Int,
        maxParticipants: Int,
    ): Flow<Result<GameEvent>> = ResultFlow.onlineOnly {
        remoteDataSource.createRegularGame(
            CreateRegularGameRequestDto(
                gameId = gameId,
                title = title,
                description = description,
                startsAt = startsAt.toString(),
                durationMinutes = durationMinutes,
                minParticipants = minParticipants,
                maxParticipants = maxParticipants,
            )
        ).toGameEvent()
    }

    override fun join(eventId: String): Flow<Result<GameEvent>> =
        ResultFlow.onlineOnly { remoteDataSource.joinEvent(eventId).toGameEvent() }

    override fun leave(eventId: String): Flow<Result<Unit>> =
        ResultFlow.onlineOnly { remoteDataSource.leaveEvent(eventId) }

    override fun cancel(eventId: String): Flow<Result<GameEvent>> =
        ResultFlow.onlineOnly { remoteDataSource.cancelEvent(eventId).toGameEvent() }
}
