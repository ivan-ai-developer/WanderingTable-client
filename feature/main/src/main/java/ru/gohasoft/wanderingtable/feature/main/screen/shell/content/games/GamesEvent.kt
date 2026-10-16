package ru.gohasoft.wanderingtable.feature.main.screen.shell.content.games

internal sealed interface GamesEvent {
    data class OnFilterSelected(val filter: GamesFilter) : GamesEvent
    data class OnGameClick(val eventId: String) : GamesEvent
    data class OnJoinClick(val eventId: String) : GamesEvent
    data object OnRetryClick : GamesEvent
}
