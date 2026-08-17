package ru.gohasoft.wanderingtable.data.auth.local.dbo

import kotlinx.serialization.Serializable

/**
 * The signed-in user as cached on disk, so the app can render a session before `GET /users/me`
 * answers (or while it never does, offline). Not a credential, so unlike the tokens it is stored
 * in the clear.
 */
@Serializable
internal data class CachedUserDbo(
    val id: String,
    val name: String,
    val email: String,
    val roles: List<String> = emptyList(),
)
