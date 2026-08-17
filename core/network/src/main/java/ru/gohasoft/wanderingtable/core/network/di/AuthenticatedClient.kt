package ru.gohasoft.wanderingtable.core.network.di

import javax.inject.Qualifier

/** The client that attaches `Bearer` tokens and transparently refreshes them on 401. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthenticatedClient
