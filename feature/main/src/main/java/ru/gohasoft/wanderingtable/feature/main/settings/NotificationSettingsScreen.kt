package ru.gohasoft.wanderingtable.feature.main.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.list.MenuRow
import ru.gohasoft.wanderingtable.core.uikit.components.section.SectionCaption
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.toggle.ToggleSwitch
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R

@Serializable
internal data object NotificationSettingsScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<NotificationSettingsViewModel>()) { state ->
            NotificationSettingsContent(state, ::onEvent)
        }
    }
}

@Composable
private fun NotificationSettingsContent(
    state: NotificationSettingsState,
    onEvent: (NotificationSettingsEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(NotificationSettingsEvent.OnBackClick) },
            title = stringResource(R.string.notification_settings_title),
            backContentDescription = stringResource(R.string.main_action_back),
        )

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            item(key = "master") {
                SettingCard(
                    title = stringResource(R.string.notification_settings_push_title),
                    subtitle = stringResource(R.string.notification_settings_push_subtitle),
                    checked = state.pushEnabled,
                    onCheckedChange = { onEvent(NotificationSettingsEvent.OnPushEnabledChanged(it)) },
                )
            }

            item(key = "activity_header") {
                SectionCaption(stringResource(R.string.notification_settings_section_activity))
            }
            item(key = "invites") {
                SettingCard(
                    title = stringResource(R.string.notification_settings_invites),
                    checked = state.gameInvites,
                    enabled = state.pushEnabled,
                    onCheckedChange = { onEvent(NotificationSettingsEvent.OnGameInvitesChanged(it)) },
                )
            }
            item(key = "news") {
                SettingCard(
                    title = stringResource(R.string.notification_settings_news),
                    checked = state.clubNews,
                    enabled = state.pushEnabled,
                    onCheckedChange = { onEvent(NotificationSettingsEvent.OnClubNewsChanged(it)) },
                )
            }
            item(key = "reminders") {
                SettingCard(
                    title = stringResource(R.string.notification_settings_reminders),
                    checked = state.gameReminders,
                    enabled = state.pushEnabled,
                    onCheckedChange = { onEvent(NotificationSettingsEvent.OnGameRemindersChanged(it)) },
                )
            }

            item(key = "watched_header") {
                SectionCaption(stringResource(R.string.notification_settings_section_watched))
            }
            items(items = state.watchedGames, key = WatchedGameUi::id) { game ->
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = game.name,
                    onClick = { onEvent(NotificationSettingsEvent.OnWatchedGameRemoved(game.id)) },
                )
            }
            item(key = "add_game") {
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.notification_settings_add_game),
                    onClick = { onEvent(NotificationSettingsEvent.OnAddGameClick) },
                )
            }
        }
    }

    if (state.isGamePickerVisible) {
        GamePickerSheet(
            games = state.pickableGames,
            onGamePicked = { gameId -> onEvent(NotificationSettingsEvent.OnGamePicked(gameId)) },
            onDismiss = { onEvent(NotificationSettingsEvent.OnGamePickerDismissed) },
        )
    }
}

@Composable
private fun SettingCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(WanderingTableRadius.m),
            )
            .padding(WanderingTableSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        // The master switch gates the rest: with push off, the per-topic switches do nothing.
        ToggleSwitch(
            checked = checked && enabled,
            onCheckedChange = { value -> if (enabled) onCheckedChange(value) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamePickerSheet(
    games: List<WatchedGameUi>,
    onGamePicked: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        LazyColumn(
            modifier = Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = WanderingTableSpacing.l,
                end = WanderingTableSpacing.l,
                bottom = WanderingTableSpacing.l,
            ),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(R.string.notification_settings_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(items = games, key = WatchedGameUi::id) { game ->
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = game.name,
                    onClick = { onGamePicked(game.id) },
                )
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationSettingsContentPreview() {
    WanderingTableTheme {
        NotificationSettingsContent(
            state = NotificationSettingsState(
                isLoading = false,
                watchedGames = listOf(
                    WatchedGameUi(id = "1", name = "Settlers of Catan"),
                    WatchedGameUi(id = "2", name = "Chess"),
                ),
            ),
            onEvent = {},
        )
    }
}
