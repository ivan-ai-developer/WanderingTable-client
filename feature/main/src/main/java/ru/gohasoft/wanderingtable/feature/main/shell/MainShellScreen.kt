package ru.gohasoft.wanderingtable.feature.main.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.navigation.BottomNavBar
import ru.gohasoft.wanderingtable.core.uikit.components.navigation.BottomNavItem
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.games.GamesContent
import ru.gohasoft.wanderingtable.feature.main.home.HomeContent
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileContent

/**
 * The one entry the back stack holds while the user is inside the app. Tabs live inside it as
 * composables, so switching tabs is state rather than navigation, and the system back button
 * leaves the app instead of unwinding through tabs.
 */
@Serializable
internal data object MainShellScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<MainShellViewModel>()) { state ->
            MainShellContent(state, ::onEvent)
        }
    }
}

/** Room for the floating pill so the last list item is not hidden behind it. */
private val BottomBarInset = 96.dp

@Composable
private fun MainShellContent(
    state: MainShellState,
    onEvent: (MainShellEvent) -> Unit,
) {
    val contentPadding = PaddingValues(bottom = BottomBarInset)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (state.tab) {
            MainTab.HOME -> HomeContent(
                hasUnreadNotifications = state.hasUnreadNotifications,
                onBellClick = { onEvent(MainShellEvent.OnBellClick) },
                contentPadding = contentPadding,
            )

            MainTab.GAMES -> GamesContent(
                pendingFilter = state.pendingGamesFilter,
                onPendingFilterConsumed = { onEvent(MainShellEvent.OnGamesFilterConsumed) },
                contentPadding = contentPadding,
            )

            MainTab.PROFILE -> ProfileContent(
                onOpenGames = { filter -> onEvent(MainShellEvent.OnOpenGames(filter)) },
                contentPadding = contentPadding,
            )
        }

        BottomNavBar(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = WanderingTableSpacing.s, vertical = WanderingTableSpacing.s),
            items = mainNavItems(),
            selectedIndex = state.tab.navIndex,
            onItemSelected = { index -> onEvent(MainShellEvent.OnNavItemSelected(index)) },
        )
    }

    if (state.isCreateSheetVisible) {
        CreateChoiceSheet(
            canPostNews = state.canPostNews,
            canCreateGames = state.canCreateGames,
            onPostClubNewsClick = { onEvent(MainShellEvent.OnPostClubNewsClick) },
            onCreateGameClick = { onEvent(MainShellEvent.OnCreateGameClick) },
            onFindOpponentClick = { onEvent(MainShellEvent.OnFindOpponentClick) },
            onDismiss = { onEvent(MainShellEvent.OnCreateSheetDismissed) },
        )
    }
}

/**
 * The kit ships an English-only default list; the labels are built here so they follow the app's
 * locale. Order must match [MainTab.navIndex] and [MainTab.CREATE_NAV_INDEX].
 */
@Composable
private fun mainNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(label = stringResource(R.string.nav_home), icon = Icons.Default.Home),
    BottomNavItem(label = stringResource(R.string.nav_games), icon = Icons.AutoMirrored.Filled.List),
    BottomNavItem(
        label = stringResource(R.string.nav_create),
        icon = Icons.Default.Add,
        isCreate = true,
    ),
    BottomNavItem(label = stringResource(R.string.nav_profile), icon = Icons.Default.Person),
)

// No preview here on purpose: the shell's children resolve their own ViewModels through Hilt, so
// it cannot render outside a Hilt-managed activity. Each tab previews its own stateless content.
