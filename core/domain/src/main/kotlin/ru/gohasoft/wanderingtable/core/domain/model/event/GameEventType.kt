package ru.gohasoft.wanderingtable.core.domain.model.event

enum class GameEventType {
    /** An ordinary club play — what the "Find an Opponent" flow creates. */
    REGULAR_GAME,

    /** A play that belongs to a tournament, championship or league. */
    TOURNAMENT_GAME,

    /** A one- or two-day tournament. */
    SINGLE_TOURNAMENT,

    /** A long tournament with elimination. */
    CHAMPIONSHIP,

    /** A long point-scoring event with no fixed bracket. */
    LEAGUE,
}
