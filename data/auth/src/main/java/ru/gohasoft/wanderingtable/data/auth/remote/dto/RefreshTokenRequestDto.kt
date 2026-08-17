package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

/** Body of both `POST /auth/refresh` and `POST /auth/logout` — the server expects the same shape. */
@Serializable
internal data class RefreshTokenRequestDto(
    val refreshToken: String,
)
