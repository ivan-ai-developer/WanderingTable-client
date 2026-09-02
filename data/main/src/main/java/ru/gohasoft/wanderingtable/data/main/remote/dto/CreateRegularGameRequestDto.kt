package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/**
 * `POST /events/regular-games`. Note what is absent: the server models no skill level and no
 * table, so the matching fields on the Create Request screen stay client-side.
 */
@Serializable
internal data class CreateRegularGameRequestDto(
    val gameId: String,
    val title: String,
    val description: String?,
    val startsAt: String,
    val durationMinutes: Int,
    val minParticipants: Int,
    val maxParticipants: Int,
)
