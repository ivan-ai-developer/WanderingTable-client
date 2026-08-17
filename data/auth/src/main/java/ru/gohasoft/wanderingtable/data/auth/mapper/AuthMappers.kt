package ru.gohasoft.wanderingtable.data.auth.mapper

import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.network.auth.TokenPair
import ru.gohasoft.wanderingtable.data.auth.local.dbo.CachedUserDbo
import ru.gohasoft.wanderingtable.data.auth.remote.dto.AuthTokensResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto

internal fun AuthTokensResponseDto.toTokenPair(): TokenPair = TokenPair(
    accessToken = accessToken,
    refreshToken = refreshToken,
)

internal fun UserDto.toCachedUser(): CachedUserDbo = CachedUserDbo(
    id = id,
    name = name,
    email = email,
    roles = roles,
)

internal fun UserDto.toUser(): User = toCachedUser().toUser()

internal fun CachedUserDbo.toUser(): User = User(
    id = id,
    name = name,
    email = email,
    roles = roles.toRoles(),
)

/**
 * Unknown names are dropped rather than failing the whole mapping: the server is documented to
 * extend its role list without changing the contract, and an app that can't parse a new role
 * should still be able to log the user in.
 */
private fun List<String>.toRoles(): List<Role> =
    mapNotNull { name -> Role.entries.firstOrNull { it.name == name } }
