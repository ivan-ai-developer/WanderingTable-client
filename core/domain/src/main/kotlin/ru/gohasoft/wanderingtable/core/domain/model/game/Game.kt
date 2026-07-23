package ru.gohasoft.wanderingtable.core.domain.model.game

open class Game(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val duration: Long?
)