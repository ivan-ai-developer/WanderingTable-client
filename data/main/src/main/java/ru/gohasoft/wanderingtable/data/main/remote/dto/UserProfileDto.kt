package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/**
 * `GET /users/me`. Unlike `:data:auth`, which only needs the account, the profile screen also
 * consumes `stats`, so it is modelled here.
 */
@Serializable
internal data class UserProfileDto(
    val user: UserDto,
    val stats: UserStatsDto? = null,
)
