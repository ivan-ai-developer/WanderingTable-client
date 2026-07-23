package ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.Screen

/**
 * Base class for all Compose screens. Concrete screens MUST be `@Serializable`
 * data classes / data objects whose constructor holds only serializable args —
 * never dependencies, callbacks, ViewModels, or Compose state.
 */
@Serializable
abstract class ComposableScreen : Screen, NavKey {

    /**
     * The screen's UI. Obtain the ViewModel INSIDE this method via hiltViewModel;
     * never store it in the class.
     */
    @Composable
    abstract fun Content()
}