package ru.gohasoft.wanderingtable.data.main.local.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
internal data class NewsItemDbo(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val ownerId: String,
    /** Ordering as the server returned it, so the cached feed reads the same way. */
    val position: Int,
)
