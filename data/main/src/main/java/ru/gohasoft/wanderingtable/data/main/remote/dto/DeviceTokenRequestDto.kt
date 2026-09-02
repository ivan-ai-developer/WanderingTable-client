package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/** Body of both `PUT` and `DELETE /users/me/devices`; the delete ignores [platform]. */
@Serializable
internal data class DeviceTokenRequestDto(
    val fcmToken: String,
    val platform: String = "ANDROID",
)
