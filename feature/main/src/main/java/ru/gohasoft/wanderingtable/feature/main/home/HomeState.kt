package ru.gohasoft.wanderingtable.feature.main.home

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.main.model.GameEventUi
import ru.gohasoft.wanderingtable.feature.main.model.NewsItemUi

internal data class HomeState(
    val isLoading: Boolean = true,
    /** The soonest planned play the user is in, or null when they have none coming up. */
    val nextGame: GameEventUi? = null,
    val news: List<NewsItemUi> = emptyList(),
    val showAllNews: Boolean = false,
    val error: TextResource? = null,
) {

    /** The design shows three posts on Home; "See all" opens the rest in place. */
    val visibleNews: List<NewsItemUi>
        get() = if (showAllNews) news else news.take(PREVIEW_NEWS_COUNT)

    val canShowAllNews: Boolean
        get() = !showAllNews && news.size > PREVIEW_NEWS_COUNT

    private companion object {
        const val PREVIEW_NEWS_COUNT = 3
    }
}
