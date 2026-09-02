package ru.gohasoft.wanderingtable.feature.main.mapper

import java.time.Instant
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationType
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.model.GameEventUi
import ru.gohasoft.wanderingtable.feature.main.model.NewsItemUi
import ru.gohasoft.wanderingtable.feature.main.model.NotificationGroup
import ru.gohasoft.wanderingtable.feature.main.model.NotificationUi
import ru.gohasoft.wanderingtable.feature.main.model.ProfileUi

private const val NEWS_EXCERPT_LENGTH = 120

internal fun GameEvent.toGameEventUi(
    games: Map<String, Game>,
    currentUserId: String?,
    joinedEventIds: Set<String>,
): GameEventUi {
    val isMine = creatorId == currentUserId
    val isJoined = isMine || id in joinedEventIds
    return GameEventUi(
        id = id,
        // The catalogue name is what players recognise; the free-form title is the fallback.
        title = games[gameId]?.name ?: title,
        hostLine = if (isMine) {
            StringResource(R.string.games_hosted_by_you)
        } else {
            StringResource(R.string.games_hosted_by_member)
        },
        meta = StringResource(
            R.string.games_card_meta,
            listOf(
                startsAt?.toDateTimeLabel() ?: createdAt.toDateTimeLabel(),
                seatsLeft.toString(),
                maxParticipants.toString(),
            ),
        ),
        skillLabel = StringResource(R.string.games_skill_any),
        isMine = isMine,
        isJoined = isJoined,
        canJoin = !isJoined && hasFreeSeats && status == GameEventStatus.PLANNED,
    )
}

internal fun NewsItem.toNewsItemUi(currentUserId: String?): NewsItemUi = NewsItemUi(
    id = id,
    title = title,
    excerpt = content.take(NEWS_EXCERPT_LENGTH),
    content = content,
    dateLabel = createdAt.toShortDateLabel(),
    byline = if (ownerId == currentUserId) {
        StringResource(R.string.news_byline_you, listOf(createdAt.toLongDateLabel()))
    } else {
        StringResource(R.string.news_byline_club, listOf(createdAt.toLongDateLabel()))
    },
)

internal fun Notification.toNotificationUi(now: Instant = Instant.now()): NotificationUi =
    NotificationUi(
        id = id,
        initials = type.toInitials(),
        title = title,
        message = message,
        timestamp = createdAt.toRelativeLabel(now),
        isRead = isRead,
        highlighted = type == NotificationType.SESSION_REMINDER,
        group = if (createdAt.isToday(now)) NotificationGroup.TODAY else NotificationGroup.EARLIER,
    )

/** The feed has no avatars behind it, so the row's tile carries a glyph for the push kind. */
private fun NotificationType.toInitials(): String = when (this) {
    NotificationType.OPPONENT_FOUND -> "VS"
    NotificationType.SESSION_INVITE -> "IN"
    NotificationType.SESSION_REMINDER -> "!"
    NotificationType.NEWS -> "N"
    NotificationType.GENERAL -> "WT"
}

internal fun UserProfile.toProfileUi(): ProfileUi = ProfileUi(
    initials = user.name.toInitials(),
    name = user.name,
    subtitle = StringResource(
        R.string.profile_favourite_games,
        listOf(stats.favoriteGames.size.toString()),
    ),
    gamesPlayed = stats.gamesPlayed.toString(),
    wins = stats.wins.toString(),
    levelLabel = winsToLevelLabel(stats.wins),
    canPostNews = Role.NEWS_CREATOR in user.roles,
    isClubManager = Role.CLUB_MANAGER in user.roles,
)

/**
 * The design shows a word, not a number, in the profile's third tile. The server ranks players
 * only inside a league, so the club-wide label is derived from total wins.
 */
private fun winsToLevelLabel(wins: Int): TextResource = when {
    wins >= 50 -> StringResource(R.string.profile_level_master)
    wins >= 20 -> StringResource(R.string.profile_level_strategist)
    wins >= 5 -> StringResource(R.string.profile_level_regular)
    else -> StringResource(R.string.profile_level_newcomer)
}

/** Avatars are initials-only in this design; two letters at most, from the first two words. */
internal fun String.toInitials(): String = trim()
    .split(" ")
    .filter(String::isNotEmpty)
    .take(2)
    .joinToString(separator = "") { it.first().uppercase() }
    .ifEmpty { "?" }

internal fun List<Game>.byId(): Map<String, Game> = associateBy(Game::id)
