package ru.gohasoft.wanderingtable.data.main.di

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
import ru.gohasoft.wanderingtable.data.main.remote.api.DevicesApi
import ru.gohasoft.wanderingtable.data.main.remote.api.EventsApi
import ru.gohasoft.wanderingtable.data.main.remote.api.GamesApi
import ru.gohasoft.wanderingtable.data.main.remote.api.NotesApi
import ru.gohasoft.wanderingtable.data.main.remote.api.UsersApi

@Module
@InstallIn(SingletonComponent::class)
internal object MainStorageModule {

    @Provides
    @Singleton
    fun provideGamesApi(@AuthenticatedClient retrofit: Retrofit): GamesApi =
        retrofit.create(GamesApi::class.java)

    @Provides
    @Singleton
    fun provideEventsApi(@AuthenticatedClient retrofit: Retrofit): EventsApi =
        retrofit.create(EventsApi::class.java)

    @Provides
    @Singleton
    fun provideNotesApi(@AuthenticatedClient retrofit: Retrofit): NotesApi =
        retrofit.create(NotesApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(@AuthenticatedClient retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideDevicesApi(@AuthenticatedClient retrofit: Retrofit): DevicesApi =
        retrofit.create(DevicesApi::class.java)

    /** Separate file from the auth store: these preferences survive a logout. */
    @Provides
    @Singleton
    @MainPreferences
    fun provideMainDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("main_prefs")
        }
}
