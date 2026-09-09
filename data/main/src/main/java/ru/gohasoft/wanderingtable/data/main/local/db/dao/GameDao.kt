package ru.gohasoft.wanderingtable.data.main.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.data.main.local.dbo.GameDbo

@Dao
internal interface GameDao {

    /**
     * A blank [query] matches everything, so the catalogue and a search share one statement.
     * Searching locally is what makes the Games screen usable offline; the fetch behind it always
     * pulls the whole catalogue, never a filtered slice.
     */
    @Query(
        "select * from games where :query = '' or name like '%' || :query || '%' order by name collate nocase"
    )
    fun observeGames(query: String): Flow<List<GameDbo>>

    @Query("select * from games where id = :gameId")
    fun observeGame(gameId: String): Flow<GameDbo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameDbo>)

    @Query("delete from games")
    suspend fun deleteAll()

    /** The catalogue is fetched whole, so a refresh replaces it whole — deletions included. */
    @Transaction
    suspend fun replaceAll(games: List<GameDbo>) {
        deleteAll()
        insertAll(games)
    }
}
