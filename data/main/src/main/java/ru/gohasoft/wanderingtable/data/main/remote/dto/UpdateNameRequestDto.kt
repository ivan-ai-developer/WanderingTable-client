package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/** `PATCH /users/me` — the only editable field on the account. */
@Serializable
internal data class UpdateNameRequestDto(
    val name: String,
)
