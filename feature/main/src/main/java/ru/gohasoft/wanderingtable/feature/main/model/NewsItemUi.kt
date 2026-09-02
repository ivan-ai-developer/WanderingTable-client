package ru.gohasoft.wanderingtable.feature.main.model

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

/**
 * A club news post as the feed and the detail screen render it.
 *
 * The design badges posts with a category (Tournament / Announcement / Event) and shows a cover
 * image; the server stores neither, so there is no category field here and the detail screen
 * draws a placeholder in place of the cover.
 */
internal data class NewsItemUi(
    val id: String,
    val title: String,
    val excerpt: String,
    val content: String,
    val dateLabel: String,
    /** "by you" or the generic club byline — `ownerId` cannot be resolved to a name. */
    val byline: TextResource,
)
