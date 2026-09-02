package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/**
 * `PATCH /users/{id}/roles`. The list replaces the member's whole set — see
 * [ru.gohasoft.wanderingtable.core.domain.repository.UserRepository.updateRoles].
 */
@Serializable
internal data class UpdateRolesRequestDto(
    val roles: List<String>,
)
