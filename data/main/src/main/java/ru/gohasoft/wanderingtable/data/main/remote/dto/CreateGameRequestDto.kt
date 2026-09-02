package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/**
 * `POST /games`. `resultType` decides which fields a finished play of this game accepts, so it
 * cannot be changed casually once plays exist — the create form asks for it up front.
 */
@Serializable
internal data class CreateGameRequestDto(
    val name: String,
    val description: String?,
    val minPlayers: Int,
    val maxPlayers: Int,
    val resultType: String,
)
