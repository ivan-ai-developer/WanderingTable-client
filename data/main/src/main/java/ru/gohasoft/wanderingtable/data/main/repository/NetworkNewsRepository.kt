package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItem
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItems
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource

internal class NetworkNewsRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : NewsRepository {

    override fun getNews(): Flow<Result<List<NewsItem>>> =
        ResultFlow.onlineOnly { remoteDataSource.getNews().toNewsItems() }

    /**
     * The server exposes no per-post endpoint, so the detail screen is served from the feed.
     * Raising NotFound keeps the caller's error handling identical to a real 404.
     */
    override fun getNewsItem(newsId: String): Flow<Result<NewsItem>> = ResultFlow.onlineOnly {
        remoteDataSource.getNews().toNewsItems().firstOrNull { it.id == newsId }
            ?: throw NetworkException.NotFound()
    }

    override fun postNews(newsId: String?, title: String, content: String): Flow<Result<NewsItem>> =
        ResultFlow.onlineOnly { remoteDataSource.upsertNews(newsId, title, content).toNewsItem() }

    override fun deleteNews(newsId: String): Flow<Result<Unit>> =
        ResultFlow.onlineOnly { remoteDataSource.deleteNews(newsId) }
}
