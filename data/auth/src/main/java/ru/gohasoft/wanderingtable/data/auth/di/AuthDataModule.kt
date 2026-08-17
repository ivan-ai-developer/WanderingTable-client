package ru.gohasoft.wanderingtable.data.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.network.auth.TokenProvider
import ru.gohasoft.wanderingtable.core.network.auth.TokenRefresher
import ru.gohasoft.wanderingtable.data.auth.local.crypto.keystore.KeystoreTokenCipher
import ru.gohasoft.wanderingtable.data.auth.local.crypto.TokenCipher
import ru.gohasoft.wanderingtable.data.auth.repository.AuthApiTokenRefresher
import ru.gohasoft.wanderingtable.data.auth.repository.DataStoreTokenProvider
import ru.gohasoft.wanderingtable.data.auth.repository.TokenBackedAuthRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface AuthDataModule {

    @Binds
    fun bindAuthRepository(impl: TokenBackedAuthRepository): AuthRepository

    /** Fulfils `:core:network`'s hooks, which is how the token pipeline is wired without a cycle. */
    @Binds
    fun bindTokenProvider(impl: DataStoreTokenProvider): TokenProvider

    @Binds
    fun bindTokenRefresher(impl: AuthApiTokenRefresher): TokenRefresher

    @Binds
    fun bindTokenCipher(impl: KeystoreTokenCipher): TokenCipher
}
