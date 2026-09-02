package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository

internal class FakeGameRepository : GameRepository {

    var games: List<Game> = emptyList()
    var getGamesResult: Result<List<Game>>? = null
    var createGameResult: Result<Game> = Result.Success(null)
    val createdGames = mutableListOf<CreatedGame>()

    override fun getGames(query: String?): Flow<Result<List<Game>>> =
        flowOf(getGamesResult ?: Result.Success(games))

    override fun getGame(gameId: String): Flow<Result<Game>> =
        flowOf(Result.Success(games.firstOrNull { it.id == gameId }))

    override fun createGame(
        name: String,
        description: String?,
        minPlayers: Int,
        maxPlayers: Int,
        resultType: GameResultType,
    ): Flow<Result<Game>> {
        createdGames += CreatedGame(name, description, minPlayers, maxPlayers, resultType)
        return flowOf(createGameResult)
    }

    /** Captures the payload so tests can assert what the form actually posted. */
    internal data class CreatedGame(
        val name: String,
        val description: String?,
        val minPlayers: Int,
        val maxPlayers: Int,
        val resultType: GameResultType,
    )
}
