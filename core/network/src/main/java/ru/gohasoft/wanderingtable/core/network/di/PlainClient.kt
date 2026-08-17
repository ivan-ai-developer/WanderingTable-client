package ru.gohasoft.wanderingtable.core.network.di

import javax.inject.Qualifier

/**
 * The client that never sends credentials. Used for the public `auth` endpoints and, crucially,
 * for the refresh call itself — a refresh made on the authenticated client would re-enter
 * [ru.gohasoft.wanderingtable.core.network.auth.TokenAuthenticator] on its own 401.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PlainClient
