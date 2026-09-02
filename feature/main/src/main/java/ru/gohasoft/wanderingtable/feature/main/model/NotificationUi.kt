package ru.gohasoft.wanderingtable.feature.main.model

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class NotificationUi(
    val id: String,
    val initials: String,
    val title: String,
    val message: String,
    val timestamp: TextResource,
    val isRead: Boolean,
    /** Reminders get the gold-tinted row, because they are the ones that expire. */
    val highlighted: Boolean,
    val group: NotificationGroup,
)
