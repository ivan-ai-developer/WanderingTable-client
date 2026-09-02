package ru.gohasoft.wanderingtable.feature.main.settings

internal sealed interface NotificationSettingsEvent {
    data object OnBackClick : NotificationSettingsEvent
    data class OnPushEnabledChanged(val enabled: Boolean) : NotificationSettingsEvent
    data class OnGameInvitesChanged(val enabled: Boolean) : NotificationSettingsEvent
    data class OnClubNewsChanged(val enabled: Boolean) : NotificationSettingsEvent
    data class OnGameRemindersChanged(val enabled: Boolean) : NotificationSettingsEvent
    data object OnAddGameClick : NotificationSettingsEvent
    data object OnGamePickerDismissed : NotificationSettingsEvent
    data class OnGamePicked(val gameId: String) : NotificationSettingsEvent
    data class OnWatchedGameRemoved(val gameId: String) : NotificationSettingsEvent
}
