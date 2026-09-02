package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<String> = emptyList(),
)
