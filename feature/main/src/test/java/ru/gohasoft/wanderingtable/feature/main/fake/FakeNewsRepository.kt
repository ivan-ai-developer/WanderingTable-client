package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository

internal class FakeNewsRepository : NewsRepository {

    var news: List<NewsItem> = emptyList()
    var getNewsResult: Result<List<NewsItem>>? = null
    var postNewsResult: Result<NewsItem> = Result.Success(null)
    var postedNews = mutableListOf<PostedNews>()

    override fun getNews(): Flow<Result<List<NewsItem>>> =
        flowOf(getNewsResult ?: Result.Success(news))

    override fun getNewsItem(newsId: String): Flow<Result<NewsItem>> =
        flowOf(Result.Success(news.firstOrNull { it.id == newsId }))

    override fun postNews(newsId: String?, title: String, content: String): Flow<Result<NewsItem>> {
        postedNews += PostedNews(newsId = newsId, title = title, content = content)
        return flowOf(postNewsResult)
    }

    override fun deleteNews(newsId: String): Flow<Result<Unit>> = flowOf(Result.Success(Unit))

    internal data class PostedNews(val newsId: String?, val title: String, val content: String)
}
