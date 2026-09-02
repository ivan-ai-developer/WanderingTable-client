package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/** An entry of the game catalogue — `GET /games`, `GET /games/{id}`. */
@Serializable
internal data class GameDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val resultType: String,
    val creatorId: String? = null,
    val createdAt: String? = null,
)
