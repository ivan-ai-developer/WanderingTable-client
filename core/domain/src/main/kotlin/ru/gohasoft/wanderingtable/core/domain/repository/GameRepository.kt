package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.game.Game

interface GameRepository {

    fun getGames(): Flow<Result<List<Game>>>

    fun getGame(gameId: String): Flow<Result<Game>>
}
