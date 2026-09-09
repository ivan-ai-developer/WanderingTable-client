package ru.gohasoft.wanderingtable.data.main.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.data.main.local.dbo.NewsItemDbo

@Dao
internal interface NewsDao {

    @Query("select * from news order by position")
    fun observeNews(): Flow<List<NewsItemDbo>>

    @Query("select * from news where id = :newsId")
    fun observeNewsItem(newsId: String): Flow<NewsItemDbo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsItemDbo>)

    @Query("delete from news")
    suspend fun deleteAll()

    /** The feed is fetched whole, so a refresh drops posts the server no longer returns. */
    @Transaction
    suspend fun replaceAll(news: List<NewsItemDbo>) {
        deleteAll()
        insertAll(news)
    }
}
