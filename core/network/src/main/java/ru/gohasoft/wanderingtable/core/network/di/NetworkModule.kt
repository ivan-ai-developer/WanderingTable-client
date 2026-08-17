package ru.gohasoft.wanderingtable.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import ru.gohasoft.wanderingtable.core.network.BuildConfig
import ru.gohasoft.wanderingtable.core.network.auth.AuthInterceptor
import ru.gohasoft.wanderingtable.core.network.auth.TokenAuthenticator

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONTENT_TYPE = "application/json"

    /** The server keeps adding response fields (stats, strategies), so unknown keys are ignored. */
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    @PlainClient
    fun providePlainOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .withDebugLogging()
        .build()

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        // Ahead of logging, so the Authorization header is visible in the log output.
        .addInterceptor(authInterceptor)
        .withDebugLogging()
        .authenticator(tokenAuthenticator)
        .build()

    @Provides
    @Singleton
    @PlainClient
    fun providePlainRetrofit(@PlainClient client: OkHttpClient, json: Json): Retrofit =
        retrofit(client, json)

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedRetrofit(
        @AuthenticatedClient client: OkHttpClient,
        json: Json,
    ): Retrofit = retrofit(client, json)

    private fun retrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
        .build()

    private fun OkHttpClient.Builder.withDebugLogging(): OkHttpClient.Builder = apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
        }
    }
}
