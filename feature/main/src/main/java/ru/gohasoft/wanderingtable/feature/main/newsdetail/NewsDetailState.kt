package ru.gohasoft.wanderingtable.feature.main.newsdetail

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.main.model.NewsItemUi

internal data class NewsDetailState(
    val isLoading: Boolean = true,
    val news: NewsItemUi? = null,
    val error: TextResource? = null,
)
