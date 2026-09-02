package ru.gohasoft.wanderingtable.feature.main.gamedetail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.avatar.RoundedSquareAvatar
import ru.gohasoft.wanderingtable.core.uikit.components.avatar.StackedAvatarGroup
import ru.gohasoft.wanderingtable.core.uikit.components.badge.TagBadge
import ru.gohasoft.wanderingtable.core.uikit.components.badge.TagBadgeVariant
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.components.list.DetailRow
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors
import ru.gohasoft.wanderingtable.feature.main.R

@Serializable
internal data class GameDetailScreen(val eventId: String) : ComposableScreen() {

    @Composable
    override fun Content() {
        val viewModel = hiltViewModel<GameDetailViewModel, GameDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(this) }
        )
        MviContent(viewModel) { state ->
            GameDetailContent(state, ::onEvent)
        }
    }
}

@Composable
private fun GameDetailContent(
    state: GameDetailState,
    onEvent: (GameDetailEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(GameDetailEvent.OnBackClick) },
            backContentDescription = stringResource(R.string.main_action_back),
        )

        when {
            state.isLoading -> LoadingState()

            !state.isLoaded -> MessageState(
                title = stringResource(R.string.game_detail_error_title),
                subtitle = state.error?.getText(),
                actionText = stringResource(R.string.main_action_retry),
                onActionClick = { onEvent(GameDetailEvent.OnRetryClick) },
            )

            else -> GameDetailBody(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun GameDetailBody(
    state: GameDetailState,
    onEvent: (GameDetailEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WanderingTableSpacing.m),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
    ) {
        state.skillLabel?.let { skill ->
            TagBadge(text = skill.getText(), variant = TagBadgeVariant.Gold)
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = state.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundedSquareAvatar(initials = state.hostInitials)
            Text(
                text = state.hostLine?.getText().orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        DetailRow(
            label = stringResource(R.string.game_detail_label_date_time),
            value = state.dateTimeLabel,
        )
        state.locationLabel?.let { location ->
            DetailRow(
                label = stringResource(R.string.game_detail_label_location),
                value = location.getText(),
            )
        }
        state.playersLabel?.let { players ->
            DetailRow(
                label = stringResource(R.string.game_detail_label_players),
                value = players.getText(),
            )
        }

        if (state.participantInitials.isNotEmpty()) {
            StackedAvatarGroup(initials = state.participantInitials.take(3), extraCount = 0)
        }

        state.note?.let { note ->
            DetailRow(label = stringResource(R.string.game_detail_label_note), value = note)
        }

        Spacer(modifier = Modifier.weight(1f))

        val actionLabel = state.action.labelRes()
        if (actionLabel != null && !state.isActionInProgress) {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(actionLabel),
                onClick = { onEvent(GameDetailEvent.OnPrimaryActionClick) },
            )
        }
        if (state.isActionInProgress) {
            LoadingState()
        }
        if (state.action == GameDetailAction.NONE) {
            Text(
                text = stringResource(R.string.game_detail_no_action),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.extendedColors.caption,
            )
        }
    }
}

private fun GameDetailAction.labelRes(): Int? = when (this) {
    GameDetailAction.NONE -> null
    GameDetailAction.JOIN -> R.string.games_join_action
    GameDetailAction.LEAVE -> R.string.game_detail_leave_action
    GameDetailAction.CANCEL -> R.string.game_detail_cancel_action
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GameDetailContentPreview() {
    WanderingTableTheme {
        GameDetailContent(
            state = GameDetailState(
                isLoading = false,
                title = "Settlers of Catan",
                hostLine = TextResource.DynamicString("Hosted by a club member"),
                hostInitials = "?",
                skillLabel = TextResource.DynamicString("Any level"),
                dateTimeLabel = "Sat, Jul 12 · 7:00 PM",
                locationLabel = TextResource.DynamicString("At the club"),
                playersLabel = TextResource.DynamicString("1 of 2 joined"),
                participantInitials = listOf("?"),
                note = "Beginners welcome — happy to walk through the rules.",
                action = GameDetailAction.JOIN,
            ),
            onEvent = {},
        )
    }
}
