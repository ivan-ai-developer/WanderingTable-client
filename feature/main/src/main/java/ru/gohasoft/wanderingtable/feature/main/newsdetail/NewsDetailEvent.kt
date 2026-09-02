package ru.gohasoft.wanderingtable.feature.main.newsdetail

internal sealed interface NewsDetailEvent {
    data object OnBackClick : NewsDetailEvent
    data object OnRetryClick : NewsDetailEvent
}
