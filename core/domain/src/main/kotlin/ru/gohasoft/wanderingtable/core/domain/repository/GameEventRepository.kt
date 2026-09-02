package ru.gohasoft.wanderingtable.core.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus

interface GameEventRepository {

    /** The club schedule: every kind of event, newest first. [status] narrows it when set. */
    fun getEvents(status: GameEventStatus? = null): Flow<Result<List<GameEvent>>>

    /** A single event, with its `participants` filled in. */
    fun getEvent(eventId: String): Flow<Result<GameEvent>>

    /** Plays the given user took part in, most recent first. */
    fun getUserGames(userId: String): Flow<Result<List<GameEvent>>>

    /** Posts a "find an opponent" request. The caller becomes its first participant. */
    fun createRegularGame(
        gameId: String,
        title: String,
        description: String?,
        startsAt: Instant,
        durationMinutes: Int,
        minParticipants: Int,
        maxParticipants: Int,
    ): Flow<Result<GameEvent>>

    /** Takes a seat. Fails with [ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.Conflict]
     *  when the event is full, already started, or the caller is already in. */
    fun join(eventId: String): Flow<Result<GameEvent>>

    /** Frees the caller's seat. The creator cannot leave — they cancel instead. */
    fun leave(eventId: String): Flow<Result<Unit>>

    /** Creator-only. The event stays readable with [GameEventStatus.CANCELLED]. */
    fun cancel(eventId: String): Flow<Result<GameEvent>>
}
