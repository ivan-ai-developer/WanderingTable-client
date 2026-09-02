package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationSettings
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationSettingsRepository

internal class FakeNotificationSettingsRepository : NotificationSettingsRepository {

    val settings = MutableStateFlow(NotificationSettings())

    override fun getSettings(): Flow<Result<NotificationSettings>> =
        settings.map { Result.Success(it) }

    override fun updateSettings(
        settings: NotificationSettings,
    ): Flow<Result<NotificationSettings>> {
        this.settings.value = settings
        return flowOf(Result.Success(settings))
    }
}
