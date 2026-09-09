package ru.gohasoft.wanderingtable.data.main.local.db

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import ru.gohasoft.wanderingtable.data.main.local.dbo.FavoriteGameDbo

/**
 * Room instantiates a converter class itself, so the JSON codec is a file-level constant rather
 * than an injected one. `ignoreUnknownKeys` matches the network codec: a cached row written by an
 * older build must not crash a newer one.
 */
private val json = Json { ignoreUnknownKeys = true }

internal class Converters {

    @TypeConverter
    fun stringListToJson(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter
    fun favoriteGamesToJson(value: List<FavoriteGameDbo>): String = json.encodeToString(value)

    @TypeConverter
    fun jsonToFavoriteGames(value: String): List<FavoriteGameDbo> = json.decodeFromString(value)
}
