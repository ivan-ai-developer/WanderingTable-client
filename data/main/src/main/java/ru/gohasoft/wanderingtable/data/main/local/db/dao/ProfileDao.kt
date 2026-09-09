package ru.gohasoft.wanderingtable.data.main.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.data.main.local.dbo.CachedProfileDbo

@Dao
internal interface ProfileDao {

    @Query("select * from profile where id = :id")
    fun observeProfile(id: String = CachedProfileDbo.SINGLETON_ID): Flow<CachedProfileDbo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(profile: CachedProfileDbo)

    @Query("delete from profile")
    suspend fun deleteAll()
}
