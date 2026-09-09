package ru.gohasoft.wanderingtable.data.main.local.dbo

import androidx.room.Entity

/**
 * A cached schedule row.
 *
 * The same event can appear in more than one list — the club schedule and the user's own plays —
 * and the two lists are fetched with different parameters, so the cache is keyed by the *query*
 * that produced it ([scope]) as well as by the event id. Refreshing a list then replaces exactly
 * the rows of that one scope, and never silently drops another list's events.
 *
 * `participants` is not stored: list responses omit it, so a cached row could only ever claim an
 * empty roster. The detail screen keeps reading it from the network.
 */
@Entity(tableName = "game_events", primaryKeys = ["scope", "id"])
internal data class GameEventDbo(
    val scope: String,
    val id: String,
    val type: String,
    val title: String,
    val description: String?,
    val gameId: String,
    val creatorId: String,
    val status: String,
    val minParticipants: Int,
    val maxParticipants: Int,
    val participantsCount: Int,
    val createdAt: Long,
    val startsAt: Long?,
    val durationMinutes: Int?,
    /** Ordering inside the scope, so the cache replays the server's order. */
    val position: Int,
)
