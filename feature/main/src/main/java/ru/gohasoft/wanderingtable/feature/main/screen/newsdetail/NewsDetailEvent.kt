package ru.gohasoft.wanderingtable.feature.main.screen.newsdetail

internal sealed interface NewsDetailEvent {
    data object OnBackClick : NewsDetailEvent
    data object OnRetryClick : NewsDetailEvent
}
