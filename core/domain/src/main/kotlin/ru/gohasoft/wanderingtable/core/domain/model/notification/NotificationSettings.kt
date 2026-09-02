package ru.gohasoft.wanderingtable.core.domain.model.notification

/**
 * Per-device push preferences. Stored locally only — the server sends every push it knows about
 * and the client decides what to surface.
 */
data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val gameInvites: Boolean = true,
    val clubNews: Boolean = true,
    val gameReminders: Boolean = true,
    /** Games the user wants to hear about when someone opens a request for them. */
    val watchedGameIds: Set<String> = emptySet(),
)
