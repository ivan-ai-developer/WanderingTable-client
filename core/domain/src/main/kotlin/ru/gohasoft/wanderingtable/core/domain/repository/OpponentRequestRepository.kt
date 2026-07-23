package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.OpponentRequest
import ru.gohasoft.wanderingtable.core.domain.model.user.SkillLevel

interface OpponentRequestRepository {

    fun getRequests(): Flow<Result<List<OpponentRequest>>>

    fun createRequest(
        gameId: String,
        skillLevel: SkillLevel,
        location: String,
    ): Flow<Result<OpponentRequest>>

    fun respondToRequest(requestId: String): Flow<Result<Unit>>
}
