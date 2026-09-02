package ru.gohasoft.wanderingtable.core.domain.model.game

/** Decides which fields a finished play of the game accepts as a per-player result. */
enum class GameResultType {
    /** Each player reports `WIN` / `LOSS` / `DRAW`. */
    WIN_LOSS,

    /** Each player reports a score; the server derives the outcome from it. */
    POINTS,

    /** Each player reports a place; the server derives the outcome from it. */
    PLACEMENT,
}
