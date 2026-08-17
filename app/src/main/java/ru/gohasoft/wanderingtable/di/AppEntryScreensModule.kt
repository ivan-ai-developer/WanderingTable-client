package ru.gohasoft.wanderingtable.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.feature.auth.AuthFeatureEnterScreen
import ru.gohasoft.wanderingtable.feature.main.MainFeatureEnterScreen

/**
 * Only `:app` may import screens from both `:feature:auth` and `:feature:main` — feature
 * modules never depend on each other, so this is the seam that resolves cross-feature
 * navigation targets (see [AppEntryScreens]).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppEntryScreensModule {

    @Binds
    abstract fun bindAppEntryScreens(impl: DefaultAppEntryScreens): AppEntryScreens
}

class DefaultAppEntryScreens @Inject constructor() : AppEntryScreens {
    override fun home(): ComposableScreen = MainFeatureEnterScreen.screen
    override fun login(): ComposableScreen = AuthFeatureEnterScreen.screen
}
