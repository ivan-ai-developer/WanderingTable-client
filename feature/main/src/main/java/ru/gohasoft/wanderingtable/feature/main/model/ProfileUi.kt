package ru.gohasoft.wanderingtable.feature.main.model

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

/**
 * The Profile header.
 *
 * The design's "Member since 2023" has no field behind it — the account carries no creation date —
 * so [subtitle] states how many games the player has favourites in instead. [levelLabel] is
 * derived from wins: the server only ranks players inside a league, not globally.
 */
internal data class ProfileUi(
    val initials: String,
    val name: String,
    val subtitle: TextResource,
    val gamesPlayed: String,
    val wins: String,
    val levelLabel: TextResource,
    val canPostNews: Boolean,
    /** Gates the Club Administration row — the only entry point to role management. */
    val isClubManager: Boolean,
)
