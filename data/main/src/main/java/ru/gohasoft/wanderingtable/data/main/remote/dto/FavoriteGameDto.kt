package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class FavoriteGameDto(
    val gameId: String,
    val name: String,
    val playedCount: Int = 0,
)
