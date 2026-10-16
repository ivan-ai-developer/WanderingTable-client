package ru.gohasoft.wanderingtable.feature.main.screen.gamedetail

internal sealed interface GameDetailEvent {
    data object OnBackClick : GameDetailEvent
    data object OnPrimaryActionClick : GameDetailEvent
    data object OnRetryClick : GameDetailEvent
}
