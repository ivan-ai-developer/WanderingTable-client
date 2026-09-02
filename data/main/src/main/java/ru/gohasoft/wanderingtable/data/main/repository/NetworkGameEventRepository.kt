package ru.gohasoft.wanderingtable.data.main.repository

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.data.main.mapper.toGameEvent
import ru.gohasoft.wanderingtable.data.main.mapper.toGameEvents
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateRegularGameRequestDto

internal class NetworkGameEventRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : GameEventRepository {

    override fun getEvents(status: GameEventStatus?): Flow<Result<List<GameEvent>>> =
        ResultFlow.onlineOnly { remoteDataSource.getEvents(status?.name).toGameEvents() }

    override fun getEvent(eventId: String): Flow<Result<GameEvent>> =
        ResultFlow.onlineOnly { remoteDataSource.getEvent(eventId).toGameEvent() }

    override fun getUserGames(userId: String): Flow<Result<List<GameEvent>>> =
        ResultFlow.onlineOnly { remoteDataSource.getUserGames(userId).toGameEvents() }

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
