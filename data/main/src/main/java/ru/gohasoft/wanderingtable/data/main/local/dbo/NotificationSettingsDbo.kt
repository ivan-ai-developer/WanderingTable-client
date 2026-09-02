package ru.gohasoft.wanderingtable.data.main.local.dbo

import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationSettingsDbo(
    val pushEnabled: Boolean = true,
    val gameInvites: Boolean = true,
    val clubNews: Boolean = true,
    val gameReminders: Boolean = true,
    val watchedGameIds: List<String> = emptyList(),
)
