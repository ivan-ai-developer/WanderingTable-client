package ru.gohasoft.wanderingtable.data.main.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameEventDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.NewsDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.ProfileDao
import ru.gohasoft.wanderingtable.data.main.local.dbo.CachedProfileDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.GameDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.GameEventDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.NewsItemDbo

/**
 * The offline copy of everything Home, Games and Profile read.
 *
 * Every table is a cache of a server response, never a source of truth: rows are only ever written
 * by a successful fetch, so `fallbackToDestructiveMigration` is the right answer to a schema
 * change — the next refresh refills the database, and nothing the user created is lost.
 */
@Database(
    entities = [
        GameDbo::class,
        GameEventDbo::class,
        NewsItemDbo::class,
        CachedProfileDbo::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
internal abstract class MainDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    abstract fun gameEventDao(): GameEventDao

    abstract fun newsDao(): NewsDao

    abstract fun profileDao(): ProfileDao

    companion object {
        const val NAME = "main.db"
    }
}
