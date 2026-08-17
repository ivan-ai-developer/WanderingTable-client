package ru.gohasoft.wanderingtable.data.auth.remote.dto

import kotlinx.serialization.Serializable

/**
 * `GET /users/me`. The response also carries a `stats` object; it is deliberately not modelled
 * here (the Json instance ignores unknown keys) — statistics belong to the profile feature, not
 * to session hydration.
 */
@Serializable
internal data class UserProfileDto(
    val user: UserDto,
)
