package ru.gohasoft.wanderingtable.data.main.local.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The cached catalogue. Always the whole catalogue — search filters these rows locally. */
@Entity(tableName = "games")
internal data class GameDbo(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val resultType: String,
)
