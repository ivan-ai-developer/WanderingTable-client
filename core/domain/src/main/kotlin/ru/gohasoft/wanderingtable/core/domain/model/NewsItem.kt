package ru.gohasoft.wanderingtable.core.domain.model

import ru.gohasoft.wanderingtable.core.domain.model.user.User

data class NewsItem(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val author: User
)
