package ru.gohasoft.wanderingtable.feature.main.notifications

import ru.gohasoft.wanderingtable.feature.main.model.NotificationGroup
import ru.gohasoft.wanderingtable.feature.main.model.NotificationUi

internal data class NotificationsState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationUi> = emptyList(),
) {

    val today: List<NotificationUi>
        get() = notifications.filter { it.group == NotificationGroup.TODAY }

    val earlier: List<NotificationUi>
        get() = notifications.filter { it.group == NotificationGroup.EARLIER }

    val isEmpty: Boolean get() = !isLoading && notifications.isEmpty()
}
