package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ResetPasswordRequestDto(
    val resetToken: String,
    val newPassword: String,
)
