package ru.gohasoft.wanderingtable.core.domain.model.game

/**
 * A board game in the club catalogue ("Carcassonne"), not a single play of it — that is
 * [ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent].
 */
data class Game(
    val id: String,
    val name: String,
    val description: String?,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val resultType: GameResultType,
)
