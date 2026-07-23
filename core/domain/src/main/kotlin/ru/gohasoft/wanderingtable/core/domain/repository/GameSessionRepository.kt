package ru.gohasoft.wanderingtable.core.domain.repository

import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.GameSession

interface GameSessionRepository {

    fun getSessions(): Flow<Result<List<GameSession>>>

    fun hostSession(
        gameId: String,
        dateTime: LocalDateTime,
        location: String,
        maxPlayers: Int,
    ): Flow<Result<GameSession>>

    fun joinSession(sessionId: String): Flow<Result<Unit>>

    fun leaveSession(sessionId: String): Flow<Result<Unit>>
}
