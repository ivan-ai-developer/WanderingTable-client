package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/**
 * One club event, whatever its kind — see `type`. `participants` is only filled in by
 * `GET /events/{id}`; list endpoints leave it null.
 */
@Serializable
internal data class EventDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String? = null,
    val gameId: String,
    val creatorId: String,
    val status: String,
    val minParticipants: Int = 0,
    val maxParticipants: Int = 0,
    val participantsCount: Int = 0,
    val createdAt: String,
    val startsAt: String? = null,
    val durationMinutes: Int? = null,
    val participants: List<String>? = null,
)
