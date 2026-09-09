package ru.gohasoft.wanderingtable.data.main.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.gohasoft.wanderingtable.core.domain.repository.CacheRepository
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
import ru.gohasoft.wanderingtable.data.main.repository.OfflineFirstGameEventRepository
import ru.gohasoft.wanderingtable.data.main.repository.OfflineFirstGameRepository
import ru.gohasoft.wanderingtable.data.main.repository.OfflineFirstNewsRepository
import ru.gohasoft.wanderingtable.data.main.repository.OfflineFirstUserRepository
import ru.gohasoft.wanderingtable.data.main.repository.RoomCacheRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface MainDataModule {

    @Binds
    fun bindGameRepository(impl: OfflineFirstGameRepository): GameRepository

    @Binds
    fun bindGameEventRepository(impl: OfflineFirstGameEventRepository): GameEventRepository

    @Binds
    fun bindNewsRepository(impl: OfflineFirstNewsRepository): NewsRepository

    @Binds
    fun bindUserRepository(impl: OfflineFirstUserRepository): UserRepository

    @Binds
    fun bindDeviceRepository(impl: NetworkDeviceRepository): DeviceRepository

    @Binds
    fun bindCacheRepository(impl: RoomCacheRepository): CacheRepository

    @Binds
    fun bindNotificationRepository(impl: DataStoreNotificationRepository): NotificationRepository

    @Binds
    fun bindNotificationSettingsRepository(
        impl: DataStoreNotificationSettingsRepository,
    ): NotificationSettingsRepository
}
