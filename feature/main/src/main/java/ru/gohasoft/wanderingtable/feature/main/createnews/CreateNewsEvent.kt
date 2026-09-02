package ru.gohasoft.wanderingtable.feature.main.createnews

internal sealed interface CreateNewsEvent {
    data object OnBackClick : CreateNewsEvent
    data class OnTitleChanged(val title: String) : CreateNewsEvent
    data class OnContentChanged(val content: String) : CreateNewsEvent
    data object OnSubmitClick : CreateNewsEvent
}
