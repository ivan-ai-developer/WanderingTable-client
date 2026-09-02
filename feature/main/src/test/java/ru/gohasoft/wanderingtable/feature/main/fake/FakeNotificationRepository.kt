package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationRepository

/** Backed by a [MutableStateFlow] so tests can assert the screen reacts to a new push. */
internal class FakeNotificationRepository : NotificationRepository {

    val notifications = MutableStateFlow<List<Notification>>(emptyList())
    var markedAsRead = mutableListOf<String>()

    override fun getNotifications(): Flow<Result<List<Notification>>> =
        notifications.map { Result.Success(it) }

    override fun markAsRead(notificationId: String): Flow<Result<Unit>> {
        markedAsRead += notificationId
        notifications.value = notifications.value.map { notification ->
            if (notification.id == notificationId) notification.copy(isRead = true) else notification
        }
        return flowOf(Result.Success(Unit))
    }

    override fun add(notification: Notification): Flow<Result<Unit>> {
        notifications.value = listOf(notification) + notifications.value
        return flowOf(Result.Success(Unit))
    }

    override fun clear(): Flow<Result<Unit>> {
        notifications.value = emptyList()
        return flowOf(Result.Success(Unit))
    }
}
