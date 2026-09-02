package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationRepository
import ru.gohasoft.wanderingtable.data.main.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationDbo
import ru.gohasoft.wanderingtable.data.main.mapper.toDbo
import ru.gohasoft.wanderingtable.data.main.mapper.toNotification

/**
 * The feed has no server side: entries appear when a push reaches this device, so it is read
 * straight from local storage and starts out empty on a fresh install.
 */
internal class DataStoreNotificationRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
) : NotificationRepository {

    override fun getNotifications(): Flow<Result<List<Notification>>> = ResultFlow.offlineOnly(
        localDataSource.getNotifications().map { stored ->
            stored.map(NotificationDbo::toNotification).sortedByDescending(Notification::createdAt)
        }
    )

    override fun markAsRead(notificationId: String): Flow<Result<Unit>> =
        ResultFlow.offlineOnly { localDataSource.markAsRead(notificationId) }

    override fun add(notification: Notification): Flow<Result<Unit>> =
        ResultFlow.offlineOnly { localDataSource.addNotification(notification.toDbo()) }

    override fun clear(): Flow<Result<Unit>> =
        ResultFlow.offlineOnly { localDataSource.clearNotifications() }
}
