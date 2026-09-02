package ru.gohasoft.wanderingtable.feature.main.home

internal sealed interface HomeEvent {
    data object OnNextGameClick : HomeEvent
    data class OnNewsClick(val newsId: String) : HomeEvent
    data object OnSeeAllNewsClick : HomeEvent
    data object OnRetryClick : HomeEvent
}
