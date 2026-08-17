package ru.gohasoft.wanderingtable.core.domain.model.user

/**
 * A user holds a *set* of these, not one. `PLAYER` is granted at registration and cannot be
 * removed; the rest are granted by a `CLUB_MANAGER`. The server may add new names over time, so
 * callers should test membership rather than assume this list is exhaustive.
 */
enum class Role {
    /** Base role: create game requests, join events. */
    PLAYER,

    /** Publish club news. */
    NEWS_CREATOR,

    /** Add board games to the catalogue. */
    GAME_CREATOR,

    /** Create tournaments, championships and leagues. */
    TOURNAMENT_CREATOR,

    /** Club manager: the only role that grants roles, and can manage any event or news item. */
    CLUB_MANAGER,
}
