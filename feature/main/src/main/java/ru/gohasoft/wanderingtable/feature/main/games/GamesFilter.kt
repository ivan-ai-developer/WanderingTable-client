package ru.gohasoft.wanderingtable.feature.main.games

/** The three chips above the Games list. */
internal enum class GamesFilter {
    /** Every planned club play. */
    ALL,

    /** Plays you could still take a seat in — not yours, not joined, and not full. */
    OPEN_REQUESTS,

    /** Plays you host or have joined. */
    MY_GAMES,
}
