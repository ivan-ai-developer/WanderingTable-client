package ru.gohasoft.wanderingtable.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.gohasoft.wanderingtable.core.domain.push.PushTokenProvider
import ru.gohasoft.wanderingtable.push.FirebasePushTokenProvider

/**
 * `:app` is the only module that sees a messaging SDK, so it is where the platform side of
 * [PushTokenProvider] is bound — the same seam `:data:auth` uses for `:core:network`'s hooks.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface PushModule {

    @Binds
    fun bindPushTokenProvider(impl: FirebasePushTokenProvider): PushTokenProvider
}
