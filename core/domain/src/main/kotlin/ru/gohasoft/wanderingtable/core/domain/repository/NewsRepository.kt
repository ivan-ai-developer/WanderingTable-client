package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.NewsItem

interface NewsRepository {

    fun getNews(): Flow<Result<List<NewsItem>>>
}
