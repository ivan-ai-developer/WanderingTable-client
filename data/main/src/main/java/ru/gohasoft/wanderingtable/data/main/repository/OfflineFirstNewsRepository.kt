package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository
import ru.gohasoft.wanderingtable.data.main.local.LocalDataSource
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItem
import ru.gohasoft.wanderingtable.data.main.mapper.toNewsItems
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource

internal class OfflineFirstNewsRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : NewsRepository {

    override fun getNews(): Flow<Result<List<NewsItem>>> = ResultFlow.offlineFirst(
        query = { localDataSource.getNews() },
        fetch = { remoteDataSource.getNews().toNewsItems() },
        saveFetchResult = { fetched, _ -> localDataSource.saveNews(fetched) },
    )

    /**
     * The server exposes no per-post endpoint, so the detail screen reads the feed — and, once the
     * feed is cached, its own row. The fetch refreshes the whole feed because that is the only
     * request there is; a post that is gone from it leaves the cached row deleted, and the query
     * then emits `null` for "no such post".
     *
     * A missing post reaches the caller as NotFound, keeping its error handling identical to a
     * real 404 — but only when there is nothing cached to show, so being offline is not a 404.
     */
    override fun getNewsItem(newsId: String): Flow<Result<NewsItem>> = ResultFlow.offlineFirst(
        query = { localDataSource.getNewsItem(newsId) },
        fetch = { remoteDataSource.getNews().toNewsItems() },
        // The feed is stored before the post is looked for, so a post that is gone leaves no
        // stale row behind and the query underneath this error has already emitted null.
        saveFetchResult = { fetched, _ ->
            localDataSource.saveNews(fetched)
            if (fetched.none { it.id == newsId }) throw NetworkException.NotFound()
        },
    )

    override fun postNews(newsId: String?, title: String, content: String): Flow<Result<NewsItem>> =
        ResultFlow.onlineOnly { remoteDataSource.upsertNews(newsId, title, content).toNewsItem() }

    override fun deleteNews(newsId: String): Flow<Result<Unit>> =
        ResultFlow.onlineOnly { remoteDataSource.deleteNews(newsId) }
}
