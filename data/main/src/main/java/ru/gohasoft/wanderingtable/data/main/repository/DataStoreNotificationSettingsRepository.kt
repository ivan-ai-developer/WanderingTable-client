package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationSettings
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationSettingsRepository
import ru.gohasoft.wanderingtable.data.main.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.main.mapper.toDbo
import ru.gohasoft.wanderingtable.data.main.mapper.toSettings

internal class DataStoreNotificationSettingsRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
) : NotificationSettingsRepository {

    override fun getSettings(): Flow<Result<NotificationSettings>> =
        ResultFlow.offlineOnly(localDataSource.getSettings().map { it.toSettings() })

    override fun updateSettings(
        settings: NotificationSettings,
    ): Flow<Result<NotificationSettings>> = ResultFlow.offlineOnly {
        localDataSource.saveSettings(settings.toDbo())
        settings
    }
}
