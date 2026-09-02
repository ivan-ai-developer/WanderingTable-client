package ru.gohasoft.wanderingtable.feature.main.shell

import ru.gohasoft.wanderingtable.feature.main.games.GamesFilter

internal sealed interface MainShellEvent {
    /** A tap on the bottom bar. Index 2 is Create, which opens the sheet rather than a tab. */
    data class OnNavItemSelected(val navIndex: Int) : MainShellEvent

    data object OnCreateSheetDismissed : MainShellEvent

    data object OnPostClubNewsClick : MainShellEvent

    data object OnCreateGameClick : MainShellEvent

    data object OnFindOpponentClick : MainShellEvent

    data object OnBellClick : MainShellEvent

    /** Raised by a tab that wants Games opened on a particular chip. */
    data class OnOpenGames(val filter: GamesFilter) : MainShellEvent

    data object OnGamesFilterConsumed : MainShellEvent
}
