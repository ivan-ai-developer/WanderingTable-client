package ru.gohasoft.wanderingtable.data.auth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import ru.gohasoft.wanderingtable.core.network.di.AuthenticatedClient
import ru.gohasoft.wanderingtable.core.network.di.PlainClient
import ru.gohasoft.wanderingtable.data.auth.remote.api.AuthApi
import ru.gohasoft.wanderingtable.data.auth.remote.api.SessionApi

@Module
@InstallIn(SingletonComponent::class)
internal object AuthStorageModule {

    /** Every `auth` endpoint is public, and the plain client keeps refresh out of the 401 loop. */
    @Provides
    @Singleton
    fun provideAuthApi(@PlainClient retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSessionApi(@AuthenticatedClient retrofit: Retrofit): SessionApi =
        retrofit.create(SessionApi::class.java)

    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("auth_tokens")
        }
}
