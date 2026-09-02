package ru.gohasoft.wanderingtable.feature.main.gamedetail

/** The single call to action a play offers, decided by the viewer's relation to it. */
internal enum class GameDetailAction {
    /** Full, already started, or finished — nothing to do here. */
    NONE,
    JOIN,
    LEAVE,

    /** Shown to the creator: the server refuses to let them leave their own play. */
    CANCEL,
}
