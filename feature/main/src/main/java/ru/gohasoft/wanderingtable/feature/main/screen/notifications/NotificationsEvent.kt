package ru.gohasoft.wanderingtable.feature.main.screen.notifications

internal sealed interface NotificationsEvent {
    data object OnBackClick : NotificationsEvent
    data class OnNotificationClick(val notificationId: String) : NotificationsEvent
}
