package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class UserStatsDto(
    val userId: String = "",
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val favoriteGames: List<FavoriteGameDto> = emptyList(),
)
