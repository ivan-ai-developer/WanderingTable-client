package ru.gohasoft.wanderingtable.data.main.local.dbo

import kotlinx.serialization.Serializable

/** One stored feed entry. [type] keeps the raw name so an unknown push kind is not lost on write. */
@Serializable
internal data class NotificationDbo(
    val id: String,
    val title: String,
    val message: String,
    val createdAtEpochMillis: Long,
    val isRead: Boolean,
    val type: String,
)
