package ru.gohasoft.wanderingtable.feature.main.createrequest

internal sealed interface CreateRequestEvent {
    data object OnBackClick : CreateRequestEvent
    data class OnGameSelected(val gameId: String) : CreateRequestEvent
    data object OnDateFieldClick : CreateRequestEvent
    data class OnDatePicked(val epochMillis: Long) : CreateRequestEvent
    data object OnDatePickerDismissed : CreateRequestEvent
    data object OnTimeFieldClick : CreateRequestEvent
    data class OnTimePicked(val hour: Int, val minute: Int) : CreateRequestEvent
    data object OnTimePickerDismissed : CreateRequestEvent
    data object OnPlayersIncrement : CreateRequestEvent
    data object OnPlayersDecrement : CreateRequestEvent
    data class OnSkillSelected(val skillLevel: SkillLevelUi) : CreateRequestEvent
    data object OnTableFieldClick : CreateRequestEvent
    data class OnTablePicked(val table: String) : CreateRequestEvent
    data object OnTablePickerDismissed : CreateRequestEvent
    data class OnNoteChanged(val note: String) : CreateRequestEvent
    data object OnSubmitClick : CreateRequestEvent
}
