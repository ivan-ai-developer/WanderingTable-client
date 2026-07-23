package ru.gohasoft.wanderingtable.core.presentation.navigation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3.Navigation3Router
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router

@Module
@InstallIn(SingletonComponent::class)
interface NavigationModule {

    @Binds
    fun bindRouter(impl: Navigation3Router): Router
}
