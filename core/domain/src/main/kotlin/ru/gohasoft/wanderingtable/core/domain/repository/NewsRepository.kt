package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem

interface NewsRepository {

    fun getNews(): Flow<Result<List<NewsItem>>>

    /**
     * A single post. The server exposes no per-post endpoint, so this reads the feed and picks
     * the match — it fails with `NotFound` when the id is not in it.
     */
    fun getNewsItem(newsId: String): Flow<Result<NewsItem>>

    /** Upsert: passing an existing [newsId] edits that post, `null` creates one. */
    fun postNews(newsId: String?, title: String, content: String): Flow<Result<NewsItem>>

    fun deleteNews(newsId: String): Flow<Result<Unit>>
}
