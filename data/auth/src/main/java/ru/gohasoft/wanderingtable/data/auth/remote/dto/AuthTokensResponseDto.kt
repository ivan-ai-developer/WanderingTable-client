package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthTokensResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
