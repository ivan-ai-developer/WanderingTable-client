package ru.gohasoft.wanderingtable.data.main.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import ru.gohasoft.wanderingtable.core.network.di.AuthenticatedClient
import ru.gohasoft.wanderingtable.data.main.local.db.MainDatabase
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.GameEventDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.NewsDao
import ru.gohasoft.wanderingtable.data.main.local.db.dao.ProfileDao
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

    /**
     * Every table is a cache of a server response, so a schema change may simply drop the file:
     * the next refresh refills it, and there is nothing here the user authored.
     */
    @Provides
    @Singleton
    fun provideMainDatabase(@ApplicationContext context: Context): MainDatabase =
        Room.databaseBuilder(context, MainDatabase::class.java, MainDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideGameDao(database: MainDatabase): GameDao = database.gameDao()

    @Provides
    fun provideGameEventDao(database: MainDatabase): GameEventDao = database.gameEventDao()

    @Provides
    fun provideNewsDao(database: MainDatabase): NewsDao = database.newsDao()

    @Provides
    fun provideProfileDao(database: MainDatabase): ProfileDao = database.profileDao()

    /** Separate file from the auth store: these preferences survive a logout. */
    @Provides
    @Singleton
    @MainPreferences
    fun provideMainDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("main_prefs")
        }
}
