package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationSettings

interface NotificationSettingsRepository {

    fun getSettings(): Flow<Result<NotificationSettings>>

    fun updateSettings(settings: NotificationSettings): Flow<Result<NotificationSettings>>
}
