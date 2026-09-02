package ru.gohasoft.wanderingtable.core.domain.model.news

import java.time.Instant

/**
 * A club news post. The server stores only these fields — there is no category, cover image or
 * author name behind [ownerId], so the UI resolves the author as "you" or a generic club label.
 */
data class NewsItem(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val ownerId: String,
)
