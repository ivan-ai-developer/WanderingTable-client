package ru.gohasoft.wanderingtable.data.main.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.gohasoft.wanderingtable.core.domain.repository.DeviceRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationSettingsRepository
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository
import ru.gohasoft.wanderingtable.data.main.repository.DataStoreNotificationRepository
import ru.gohasoft.wanderingtable.data.main.repository.DataStoreNotificationSettingsRepository
import ru.gohasoft.wanderingtable.data.main.repository.NetworkDeviceRepository
import ru.gohasoft.wanderingtable.data.main.repository.NetworkGameEventRepository
import ru.gohasoft.wanderingtable.data.main.repository.NetworkGameRepository
import ru.gohasoft.wanderingtable.data.main.repository.NetworkNewsRepository
import ru.gohasoft.wanderingtable.data.main.repository.NetworkUserRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface MainDataModule {

    @Binds
    fun bindGameRepository(impl: NetworkGameRepository): GameRepository

    @Binds
    fun bindGameEventRepository(impl: NetworkGameEventRepository): GameEventRepository

    @Binds
    fun bindNewsRepository(impl: NetworkNewsRepository): NewsRepository

    @Binds
    fun bindUserRepository(impl: NetworkUserRepository): UserRepository

    @Binds
    fun bindDeviceRepository(impl: NetworkDeviceRepository): DeviceRepository

    @Binds
    fun bindNotificationRepository(impl: DataStoreNotificationRepository): NotificationRepository

    @Binds
    fun bindNotificationSettingsRepository(
        impl: DataStoreNotificationSettingsRepository,
    ): NotificationSettingsRepository
}
