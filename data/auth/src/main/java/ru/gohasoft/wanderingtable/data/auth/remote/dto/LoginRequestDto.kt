package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequestDto(
    val email: String,
    val password: String,
)
