package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

/** The single-use token `auth/verify-reset-code` hands back in exchange for the emailed code. */
@Serializable
internal data class ResetTokenResponseDto(
    val resetToken: String,
)
