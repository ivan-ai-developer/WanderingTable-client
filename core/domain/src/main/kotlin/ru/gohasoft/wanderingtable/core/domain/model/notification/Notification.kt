package ru.gohasoft.wanderingtable.core.domain.model.notification

import java.time.Instant

/**
 * A single entry in the local notification feed. The server has no notification endpoints: entries
 * are appended when a push arrives, so this feed is per-device and starts out empty.
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val createdAt: Instant,
    val isRead: Boolean,
    val type: NotificationType,
)
