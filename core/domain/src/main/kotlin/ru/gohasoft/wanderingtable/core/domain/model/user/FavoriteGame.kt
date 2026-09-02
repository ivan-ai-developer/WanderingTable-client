package ru.gohasoft.wanderingtable.core.domain.model.user

data class FavoriteGame(
    val gameId: String,
    val name: String,
    val playedCount: Int,
)
