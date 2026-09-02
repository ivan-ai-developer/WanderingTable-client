package ru.gohasoft.wanderingtable.feature.main.games

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.PlainTitleBar
import ru.gohasoft.wanderingtable.core.uikit.components.card.ListCard
import ru.gohasoft.wanderingtable.core.uikit.components.card.ListCardVariant
import ru.gohasoft.wanderingtable.core.uikit.components.chip.WtFilterChip
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.model.GameEventUi

/**
 * The Games tab. [pendingFilter] is how Profile opens this tab on a specific chip; it is applied
 * once and then handed back so the shell can clear it.
 */
@Composable
internal fun GamesContent(
    pendingFilter: GamesFilter?,
    onPendingFilterConsumed: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<GamesViewModel>()
    LaunchedEffect(pendingFilter) {
        if (pendingFilter != null) {
            viewModel.applyFilter(pendingFilter)
            onPendingFilterConsumed()
        }
    }
    MviContent(viewModel) { state ->
        GamesScreenContent(
            state = state,
            onEvent = ::onEvent,
            contentPadding = contentPadding,
            modifier = modifier,
        )
    }
}

@Composable
private fun GamesScreenContent(
    state: GamesState,
    onEvent: (GamesEvent) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        PlainTitleBar(title = stringResource(R.string.games_title))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = WanderingTableSpacing.m, vertical = WanderingTableSpacing.s),
            horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        ) {
            GamesFilter.entries.forEach { filter ->
                WtFilterChip(
                    text = stringResource(filter.labelRes()),
                    selected = state.filter == filter,
                    onClick = { onEvent(GamesEvent.OnFilterSelected(filter)) },
                )
            }
        }

        when {
            state.isLoading -> LoadingState()

            state.error != null -> MessageState(
                title = stringResource(R.string.games_error_title),
                subtitle = state.error.getText(),
                actionText = stringResource(R.string.main_action_retry),
                onActionClick = { onEvent(GamesEvent.OnRetryClick) },
            )

            state.visibleGames.isEmpty() -> MessageState(
                title = stringResource(R.string.games_empty_title),
                subtitle = stringResource(state.filter.emptySubtitleRes()),
            )

            else -> GamesList(state = state, onEvent = onEvent, contentPadding = contentPadding)
        }
    }
}

@Composable
private fun GamesList(
    state: GamesState,
    onEvent: (GamesEvent) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = WanderingTableSpacing.m,
            end = WanderingTableSpacing.m,
            top = WanderingTableSpacing.s,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
    ) {
        items(items = state.visibleGames, key = GameEventUi::id) { game ->
            ListCard(
                modifier = Modifier.fillMaxWidth(),
                title = game.title,
                hostLine = game.hostLine.getText(),
                meta = game.meta.getText(),
                badgeText = game.skillLabel.getText(),
                // A play you host reads as yours and offers no action, exactly as in the design.
                variant = if (game.isMine) ListCardVariant.Highlighted else ListCardVariant.Outlined,
                ctaText = stringResource(R.string.games_join_action).takeIf {
                    game.canJoin && state.joiningEventId == null
                },
                onCtaClick = { onEvent(GamesEvent.OnJoinClick(game.id)) },
                onClick = { onEvent(GamesEvent.OnGameClick(game.id)) },
            )
        }
    }
}

private fun GamesFilter.labelRes(): Int = when (this) {
    GamesFilter.ALL -> R.string.games_filter_all
    GamesFilter.OPEN_REQUESTS -> R.string.games_filter_open_requests
    GamesFilter.MY_GAMES -> R.string.games_filter_my_games
}

private fun GamesFilter.emptySubtitleRes(): Int = when (this) {
    GamesFilter.ALL -> R.string.games_empty_all
    GamesFilter.OPEN_REQUESTS -> R.string.games_empty_open_requests
    GamesFilter.MY_GAMES -> R.string.games_empty_my_games
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GamesScreenContentPreview() {
    WanderingTableTheme {
        GamesScreenContent(
            state = GamesState(
                isLoading = false,
                games = listOf(
                    GameEventUi(
                        id = "1",
                        title = "Settlers of Catan",
                        hostLine = TextResource.DynamicString("Hosted by a club member"),
                        meta = TextResource.DynamicString("Sat, Jul 12 · 7:00 PM · needs 1 of 2"),
                        skillLabel = TextResource.DynamicString("Any level"),
                        isMine = false,
                        isJoined = false,
                        canJoin = true,
                    ),
                    GameEventUi(
                        id = "2",
                        title = "Terraforming Mars",
                        hostLine = TextResource.DynamicString("Hosted by you"),
                        meta = TextResource.DynamicString("Jul 14 · 2:00 PM · needs 1 of 3"),
                        skillLabel = TextResource.DynamicString("Any level"),
                        isMine = true,
                        isJoined = true,
                        canJoin = false,
                    ),
                ),
            ),
            onEvent = {},
            contentPadding = PaddingValues(),
        )
    }
}
