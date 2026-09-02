package ru.gohasoft.wanderingtable.feature.main.gamedetail

internal sealed interface GameDetailEvent {
    data object OnBackClick : GameDetailEvent
    data object OnPrimaryActionClick : GameDetailEvent
    data object OnRetryClick : GameDetailEvent
}
