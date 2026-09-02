package ru.gohasoft.wanderingtable.feature.main.profile

internal sealed interface ProfileEvent {
    data object OnNotificationSettingsClick : ProfileEvent

    /** Club managers only — the row is not rendered for anyone else. */
    data object OnClubAdminClick : ProfileEvent

    /** Designed but not built: the server exposes nothing behind these two rows yet. */
    data object OnAccountSettingsClick : ProfileEvent
    data object OnClubMembershipClick : ProfileEvent

    data object OnLogOutClick : ProfileEvent
    data object OnRetryClick : ProfileEvent
}
