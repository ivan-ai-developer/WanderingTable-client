package ru.gohasoft.wanderingtable.core.presentation.navigation

import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen

/**
 * Resolves the app's cross-feature entry points. Feature modules must never depend on each
 * other directly, so a screen owned by one feature (e.g. the post-login destination) is reached
 * through this seam instead of an import. Bound in `:app`, the only module allowed to see every
 * feature module.
 */
interface AppEntryScreens {

    /** The screen shown once a session is known to exist. */
    fun home(): ComposableScreen

    /** The screen shown when there is no active session. */
    fun login(): ComposableScreen
}
