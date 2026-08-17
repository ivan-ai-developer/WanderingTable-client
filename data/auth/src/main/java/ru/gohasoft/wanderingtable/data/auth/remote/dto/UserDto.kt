package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

/** Returned by `POST /auth/register` and nested in `GET /users/me`. */
@Serializable
internal data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<String> = emptyList(),
)
