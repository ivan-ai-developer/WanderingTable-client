package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/** `PUT /users/me/devices` echoes the registration back — without the token itself. */
@Serializable
internal data class DeviceDto(
    val id: String,
    val platform: String,
    val updatedAt: String? = null,
)
