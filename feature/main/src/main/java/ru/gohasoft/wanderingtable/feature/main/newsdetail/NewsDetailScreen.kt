package ru.gohasoft.wanderingtable.feature.main.newsdetail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.placeholder.CoverPlaceholder
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.model.NewsItemUi

@Serializable
internal data class NewsDetailScreen(val newsId: String) : ComposableScreen() {

    @Composable
    override fun Content() {
        val viewModel = hiltViewModel<NewsDetailViewModel, NewsDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(this) }
        )
        MviContent(viewModel) { state ->
            NewsDetailContent(state, ::onEvent)
        }
    }
}

@Composable
private fun NewsDetailContent(
    state: NewsDetailState,
    onEvent: (NewsDetailEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(NewsDetailEvent.OnBackClick) },
            backContentDescription = stringResource(R.string.main_action_back),
        )

        val news = state.news
        when {
            state.isLoading -> LoadingState()

            news == null -> MessageState(
                title = stringResource(R.string.news_detail_error_title),
                subtitle = state.error?.getText(),
                actionText = stringResource(R.string.main_action_retry),
                onActionClick = { onEvent(NewsDetailEvent.OnRetryClick) },
            )

            else -> NewsDetailBody(news)
        }
    }
}

@Composable
private fun NewsDetailBody(news: NewsItemUi) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WanderingTableSpacing.m),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
    ) {
        // Posts carry no image server-side; the design's cover slot keeps its place as a hatch.
        CoverPlaceholder(label = stringResource(R.string.news_detail_cover_placeholder))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = news.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = news.byline.getText(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.extendedColors.caption,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = news.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 22.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NewsDetailContentPreview() {
    WanderingTableTheme {
        NewsDetailContent(
            state = NewsDetailState(
                isLoading = false,
                news = NewsItemUi(
                    id = "1",
                    title = "Spring Team Championship — Registration Open",
                    excerpt = "",
                    content = "Sign up in teams of 2 for our biggest tournament of the season. " +
                        "Matches run every weekend through August, with prizes for the top three.",
                    dateLabel = "Jul 14",
                    byline = TextResource.DynamicString("Posted July 14, 2026 · by the club"),
                ),
            ),
            onEvent = {},
        )
    }
}
