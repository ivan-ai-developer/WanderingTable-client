package ru.gohasoft.wanderingtable.core.domain.model

import java.time.Instant

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val createdAt: Instant,
    val isRead: Boolean,
    val type: NotificationType,
)

enum class NotificationType {
    OPPONENT_FOUND,
    SESSION_INVITE,
    SESSION_REMINDER,
    NEWS,
    GENERAL,
}
