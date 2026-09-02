package ru.gohasoft.wanderingtable.feature.main.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BrandedTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.card.HeroCard
import ru.gohasoft.wanderingtable.core.uikit.components.card.NewsCard
import ru.gohasoft.wanderingtable.core.uikit.components.section.SectionHeader
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.model.NewsItemUi

/**
 * The Home tab. It is not a [ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen]:
 * tabs live inside `MainShellScreen`, which owns the single back-stack entry.
 */
@Composable
internal fun HomeContent(
    hasUnreadNotifications: Boolean,
    onBellClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    MviContent(hiltViewModel<HomeViewModel>()) { state ->
        HomeScreenContent(
            state = state,
            onEvent = ::onEvent,
            hasUnreadNotifications = hasUnreadNotifications,
            onBellClick = onBellClick,
            contentPadding = contentPadding,
            modifier = modifier,
        )
    }
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    hasUnreadNotifications: Boolean,
    onBellClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        BrandedTopBar(
            title = stringResource(R.string.app_title),
            onBellClick = onBellClick,
            hasUnread = hasUnreadNotifications,
            bellContentDescription = stringResource(R.string.home_notifications_action),
        )

        when {
            state.isLoading -> LoadingState()

            state.error != null -> MessageState(
                title = stringResource(R.string.home_error_title),
                subtitle = state.error.getText(),
                actionText = stringResource(R.string.main_action_retry),
                onActionClick = { onEvent(HomeEvent.OnRetryClick) },
            )

            else -> HomeFeed(state = state, onEvent = onEvent, contentPadding = contentPadding)
        }
    }
}

@Composable
private fun HomeFeed(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
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
        item(key = "hero") {
            val nextGame = state.nextGame
            if (nextGame == null) {
                MessageState(
                    title = stringResource(R.string.home_no_upcoming_title),
                    subtitle = stringResource(R.string.home_no_upcoming_subtitle),
                )
            } else {
                HeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    eyebrow = stringResource(R.string.home_hero_eyebrow),
                    title = nextGame.title,
                    meta = nextGame.meta.getText(),
                    onClick = { onEvent(HomeEvent.OnNextGameClick) },
                )
            }
        }

        item(key = "news_header") {
            SectionHeader(
                title = stringResource(R.string.home_news_title),
                actionText = stringResource(R.string.home_news_see_all).takeIf { state.canShowAllNews },
                onActionClick = { onEvent(HomeEvent.OnSeeAllNewsClick) },
            )
        }

        if (state.news.isEmpty()) {
            item(key = "news_empty") {
                MessageState(
                    title = stringResource(R.string.home_no_news_title),
                    subtitle = stringResource(R.string.home_no_news_subtitle),
                )
            }
        } else {
            items(items = state.visibleNews, key = NewsItemUi::id) { item ->
                NewsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    excerpt = item.excerpt,
                    date = item.dateLabel,
                    onClick = { onEvent(HomeEvent.OnNewsClick(item.id)) },
                )
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenContentPreview() {
    WanderingTableTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                news = listOf(
                    NewsItemUi(
                        id = "1",
                        title = "Spring Team Championship — Registration Open",
                        excerpt = "Sign up in teams of 2 for our biggest tournament of the season.",
                        content = "",
                        dateLabel = "Jul 14",
                        byline = TextResource.DynamicString("Posted July 14, 2026"),
                    ),
                ),
            ),
            onEvent = {},
            hasUnreadNotifications = true,
            onBellClick = {},
            contentPadding = PaddingValues(),
        )
    }
}
