package ru.gohasoft.wanderingtable.feature.main.fake

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen

internal class FakeAppEntryScreens : AppEntryScreens {
    override fun home(): ComposableScreen = FakeHomeScreen
    override fun login(): ComposableScreen = FakeLoginScreen
}

@Serializable
private data object FakeHomeScreen : ComposableScreen() {
    @Composable
    override fun Content() = Unit
}

@Serializable
private data object FakeLoginScreen : ComposableScreen() {
    @Composable
    override fun Content() = Unit
}
