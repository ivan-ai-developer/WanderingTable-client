package ru.gohasoft.wanderingtable.data.main.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.data.main.local.dbo.GameEventDbo

@Dao
internal interface GameEventDao {

    @Query("select * from game_events where scope = :scope order by position")
    fun observeEvents(scope: String): Flow<List<GameEventDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<GameEventDbo>)

    @Query("delete from game_events where scope = :scope")
    suspend fun deleteScope(scope: String)

    @Query("delete from game_events")
    suspend fun deleteAll()

    /** Only the refreshed scope is replaced — the other lists' rows stay where they are. */
    @Transaction
    suspend fun replaceScope(scope: String, events: List<GameEventDbo>) {
        deleteScope(scope)
        insertAll(events)
    }
}
