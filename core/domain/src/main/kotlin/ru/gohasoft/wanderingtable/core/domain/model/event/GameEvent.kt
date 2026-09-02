package ru.gohasoft.wanderingtable.core.domain.model.event

import java.time.Instant

/**
 * Anything that happens at the club: a single play, a tournament, or a league. All kinds share
 * one shape, one id space, and the join / start / cancel operations; [type] says which kind it is.
 *
 * A "find an opponent" request is a [GameEventType.REGULAR_GAME] whose creator is its first
 * participant — there is no separate entity for it.
 */
data class GameEvent(
    val id: String,
    val type: GameEventType,
    val title: String,
    val description: String?,
    val gameId: String,
    val creatorId: String,
    val status: GameEventStatus,
    val minParticipants: Int,
    val maxParticipants: Int,
    val participantsCount: Int,
    val createdAt: Instant,
    /** Only plays carry a start time; tournaments schedule their own games instead. */
    val startsAt: Instant?,
    val durationMinutes: Int?,
    /** Ids of the participants. Only populated when a single event is loaded, `null` in lists. */
    val participants: List<String>?,
) {

    val hasFreeSeats: Boolean get() = participantsCount < maxParticipants

    val seatsLeft: Int get() = (maxParticipants - participantsCount).coerceAtLeast(0)
}
