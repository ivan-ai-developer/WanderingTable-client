package ru.gohasoft.wanderingtable.feature.main.shell

import ru.gohasoft.wanderingtable.feature.main.games.GamesFilter

internal data class MainShellState(
    val tab: MainTab = MainTab.HOME,
    val isCreateSheetVisible: Boolean = false,
    /**
     * The Create sheet only offers what this account may actually do, so no option can end in
     * a 403. Both mirror the role the matching endpoint requires: `NEWS_CREATOR` for
     * `POST /notes`, `GAME_CREATOR` for `POST /games`.
     */
    val canPostNews: Boolean = false,
    val canCreateGames: Boolean = false,
    val hasUnreadNotifications: Boolean = false,
    /**
     * Set when another tab asks Games to open on a particular chip — Profile's "My Games" and
     * "Find Opponent Requests" rows. Cleared once the Games tab has applied it.
     */
    val pendingGamesFilter: GamesFilter? = null,
)
