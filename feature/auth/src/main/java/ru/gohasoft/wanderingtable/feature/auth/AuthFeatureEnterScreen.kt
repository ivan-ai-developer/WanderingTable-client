package ru.gohasoft.wanderingtable.feature.auth

import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.feature.auth.login.LoginScreen

object AuthFeatureEnterScreen {
    val screen: ComposableScreen
        get() = LoginScreen
}