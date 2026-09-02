package ru.gohasoft.wanderingtable.feature.main.settings

internal data class NotificationSettingsState(
    val isLoading: Boolean = true,
    val pushEnabled: Boolean = true,
    val gameInvites: Boolean = true,
    val clubNews: Boolean = true,
    val gameReminders: Boolean = true,
    val watchedGames: List<WatchedGameUi> = emptyList(),
    /** Catalogue entries not yet watched — what the picker offers. */
    val pickableGames: List<WatchedGameUi> = emptyList(),
    val isGamePickerVisible: Boolean = false,
)
