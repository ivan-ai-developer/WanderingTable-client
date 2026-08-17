package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ForgotPasswordRequestDto(
    val email: String,
)
