package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification

/** The local push feed. See [Notification] for why it is device-local. */
interface NotificationRepository {

    fun getNotifications(): Flow<Result<List<Notification>>>

    fun markAsRead(notificationId: String): Flow<Result<Unit>>

    fun add(notification: Notification): Flow<Result<Unit>>

    fun clear(): Flow<Result<Unit>>
}
