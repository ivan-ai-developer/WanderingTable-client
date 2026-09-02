package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType

interface GameRepository {

    /** The club catalogue. [query] filters by a case-insensitive substring of the name. */
    fun getGames(query: String? = null): Flow<Result<List<Game>>>

    fun getGame(gameId: String): Flow<Result<Game>>

    /**
     * Adds a game to the club catalogue. Requires the
     * [ru.gohasoft.wanderingtable.core.domain.model.user.Role.GAME_CREATOR] role — callers without
     * it get [ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.Forbidden].
     *
     * Names are unique club-wide: a duplicate comes back as
     * [ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.Conflict].
     */
    fun createGame(
        name: String,
        description: String?,
        minPlayers: Int,
        maxPlayers: Int,
        resultType: GameResultType,
    ): Flow<Result<Game>>
}
