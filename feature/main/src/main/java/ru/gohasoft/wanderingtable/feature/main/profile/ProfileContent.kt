package ru.gohasoft.wanderingtable.feature.main.profile

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.header.ProfileGradientHeader
import ru.gohasoft.wanderingtable.core.uikit.components.header.ProfileStat
import ru.gohasoft.wanderingtable.core.uikit.components.list.MenuRow
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.games.GamesFilter
import ru.gohasoft.wanderingtable.feature.main.model.ProfileUi

/**
 * The Profile tab. "My Games" and "Find Opponent Requests" are handled by [onOpenGames] rather
 * than by the ViewModel: they switch tabs, which is the shell's state, not navigation.
 */
@Composable
internal fun ProfileContent(
    onOpenGames: (GamesFilter) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    MviContent(hiltViewModel<ProfileViewModel>()) { state ->
        ProfileScreenContent(
            state = state,
            onEvent = ::onEvent,
            onOpenGames = onOpenGames,
            contentPadding = contentPadding,
            modifier = modifier,
        )
    }
}

@Composable
private fun ProfileScreenContent(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    onOpenGames: (GamesFilter) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity

        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
            )
        )

        onDispose {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                )
            )
        }
    }
    val profile = state.profile
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())

            profile == null -> MessageState(
                title = stringResource(R.string.profile_error_title),
                subtitle = state.error?.getText(),
                actionText = stringResource(R.string.main_action_retry),
                onActionClick = { onEvent(ProfileEvent.OnRetryClick) },
            )

            else -> ProfileBody(
                profile = profile,
                isLoggingOut = state.isLoggingOut,
                onEvent = onEvent,
                onOpenGames = onOpenGames,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun ProfileBody(
    profile: ProfileUi,
    isLoggingOut: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    onOpenGames: (GamesFilter) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
    ) {
        item(key = "header") {
            ProfileGradientHeader(
                initials = profile.initials,
                name = profile.name,
                subtitle = profile.subtitle.getText(),
                stats = listOf(
                    ProfileStat(
                        value = profile.gamesPlayed,
                        label = stringResource(R.string.profile_stat_games),
                    ),
                    ProfileStat(
                        value = profile.wins,
                        label = stringResource(R.string.profile_stat_wins),
                    ),
                    ProfileStat(
                        value = profile.levelLabel.getText(),
                        label = stringResource(R.string.profile_stat_level),
                    ),
                ),
            )
        }

        item(key = "menu") {
            Column(
                modifier = Modifier.padding(
                    horizontal = WanderingTableSpacing.m,
                    vertical = WanderingTableSpacing.s,
                ),
                verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
            ) {
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.profile_menu_my_games),
                    onClick = { onOpenGames(GamesFilter.MY_GAMES) },
                )
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.profile_menu_opponent_requests),
                    onClick = { onOpenGames(GamesFilter.OPEN_REQUESTS) },
                )
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.profile_menu_notification_settings),
                    onClick = { onEvent(ProfileEvent.OnNotificationSettingsClick) },
                )
                // Role management has no meaning for anyone else — the endpoints behind it are
                // club-manager only.
                if (profile.isClubManager) {
                    MenuRow(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.profile_menu_club_admin),
                        onClick = { onEvent(ProfileEvent.OnClubAdminClick) },
                    )
                }
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.profile_menu_account_settings),
                    onClick = { onEvent(ProfileEvent.OnAccountSettingsClick) },
                )
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.profile_menu_club_membership),
                    onClick = { onEvent(ProfileEvent.OnClubMembershipClick) },
                )
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.profile_menu_log_out),
                    onClick = { onEvent(ProfileEvent.OnLogOutClick) },
                    destructive = true,
                )
                if (isLoggingOut) {
                    LoadingState()
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenContentPreview() {
    WanderingTableTheme {
        ProfileScreenContent(
            state = ProfileState(
                isLoading = false,
                profile = ProfileUi(
                    initials = "AN",
                    name = "Alex Novak",
                    subtitle = TextResource.DynamicString("5 favourite games"),
                    gamesPlayed = "42",
                    wins = "19",
                    levelLabel = TextResource.DynamicString("Strategist"),
                    canPostNews = true,
                    isClubManager = true,
                ),
            ),
            onEvent = {},
            onOpenGames = {},
            contentPadding = PaddingValues(),
        )
    }
}
