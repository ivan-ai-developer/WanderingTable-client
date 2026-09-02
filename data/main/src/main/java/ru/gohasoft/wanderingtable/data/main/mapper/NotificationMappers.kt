package ru.gohasoft.wanderingtable.data.main.mapper

import java.time.Instant
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationSettings
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationType
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationDbo
import ru.gohasoft.wanderingtable.data.main.local.dbo.NotificationSettingsDbo

internal fun NotificationDbo.toNotification(): Notification = Notification(
    id = id,
    title = title,
    message = message,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    isRead = isRead,
    // A push whose kind this build does not know still belongs in the feed, just untyped.
    type = NotificationType.entries.firstOrNull { it.name == type } ?: NotificationType.GENERAL,
)

internal fun Notification.toDbo(): NotificationDbo = NotificationDbo(
    id = id,
    title = title,
    message = message,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    isRead = isRead,
    type = type.name,
)

internal fun NotificationSettingsDbo.toSettings(): NotificationSettings = NotificationSettings(
    pushEnabled = pushEnabled,
    gameInvites = gameInvites,
    clubNews = clubNews,
    gameReminders = gameReminders,
    watchedGameIds = watchedGameIds.toSet(),
)

internal fun NotificationSettings.toDbo(): NotificationSettingsDbo = NotificationSettingsDbo(
    pushEnabled = pushEnabled,
    gameInvites = gameInvites,
    clubNews = clubNews,
    gameReminders = gameReminders,
    watchedGameIds = watchedGameIds.toList(),
)
