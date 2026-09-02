package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.data.main.mapper.toGame
import ru.gohasoft.wanderingtable.data.main.mapper.toGames
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateGameRequestDto

/** The catalogue is small and rarely changes, but there is no local store yet — reads go online. */
internal class NetworkGameRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : GameRepository {

    override fun getGames(query: String?): Flow<Result<List<Game>>> =
        ResultFlow.onlineOnly { remoteDataSource.getGames(query).toGames() }

    override fun getGame(gameId: String): Flow<Result<Game>> =
        ResultFlow.onlineOnly { remoteDataSource.getGame(gameId).toGame() }

    override fun createGame(
        name: String,
        description: String?,
        minPlayers: Int,
        maxPlayers: Int,
        resultType: GameResultType,
    ): Flow<Result<Game>> = ResultFlow.onlineOnly {
        remoteDataSource.createGame(
            CreateGameRequestDto(
                name = name,
                description = description,
                minPlayers = minPlayers,
                maxPlayers = maxPlayers,
                resultType = resultType.name,
            )
        ).toGame()
    }
}
