package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.data.main.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.main.mapper.toGame
import ru.gohasoft.wanderingtable.data.main.mapper.toGames
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateGameRequestDto

/**
 * The catalogue is small and rarely changes, so it is cached whole and read from Room.
 *
 * Search is answered from the cache too, and the fetch behind it always pulls the *unfiltered*
 * catalogue: fetching a filtered slice and replacing the table with it would leave the catalogue
 * looking like whatever was last searched for.
 */
internal class OfflineFirstGameRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : GameRepository {

    override fun getGames(query: String?): Flow<Result<List<Game>>> = ResultFlow.offlineFirst(
        query = { localDataSource.getGames(query) },
        fetch = { remoteDataSource.getGames(null).toGames() },
        saveFetchResult = { fetched, _ -> localDataSource.saveGames(fetched) },
    )

    override fun getGame(gameId: String): Flow<Result<Game>> = ResultFlow.offlineFirst(
        query = { localDataSource.getGame(gameId) },
        fetch = { remoteDataSource.getGame(gameId).toGame() },
        saveFetchResult = { fetched, _ -> localDataSource.upsertGames(listOf(fetched)) },
    )

    /**
     * Not cached ahead: the new game is written by the refresh the caller triggers afterwards,
     * and inserting it here would make it appear even if the request had actually failed.
     */
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
