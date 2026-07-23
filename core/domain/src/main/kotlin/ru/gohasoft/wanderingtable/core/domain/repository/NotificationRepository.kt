package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.Notification

interface NotificationRepository {

    fun getNotifications(): Flow<Result<List<Notification>>>

    fun markAsRead(notificationId: String): Flow<Result<Unit>>
}
