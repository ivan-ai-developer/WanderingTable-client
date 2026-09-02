package ru.gohasoft.wanderingtable.data.main.di

import javax.inject.Qualifier

/** Distinguishes this feature's DataStore from the one `:data:auth` keeps tokens in. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
internal annotation class MainPreferences
