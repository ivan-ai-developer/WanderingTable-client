package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.core.domain.model.user.UserStats
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository

internal class FakeUserRepository : UserRepository {

    var profile: UserProfile? = profile()
    var getProfileResult: Result<UserProfile>? = null
    var findByEmailResult: Result<User>? = null
    var updateRolesResult: Result<User>? = null
    val roleUpdates = mutableListOf<RoleUpdate>()

    override fun getProfile(): Flow<Result<UserProfile>> =
        flowOf(getProfileResult ?: Result.Success(profile))

    override fun updateName(name: String): Flow<Result<UserProfile>> =
        flowOf(Result.Success(profile))

    override fun findUserByEmail(email: String): Flow<Result<User>> =
        flowOf(findByEmailResult ?: Result.Success(null))

    override fun updateRoles(userId: String, roles: Set<Role>): Flow<Result<User>> {
        roleUpdates += RoleUpdate(userId, roles)
        // Mirrors the server: the caller's set is what the account ends up with, plus PLAYER.
        return flowOf(
            updateRolesResult ?: Result.Success(
                User(
                    id = userId,
                    name = "Member",
                    email = "member@example.com",
                    roles = (roles + Role.PLAYER).toList(),
                )
            )
        )
    }

    /** Captures the full set each call sent, since the endpoint replaces rather than adds. */
    internal data class RoleUpdate(val userId: String, val roles: Set<Role>)

    internal companion object {
        fun profile(
            id: String = "me",
            name: String = "Alex Novak",
            wins: Int = 19,
            gamesPlayed: Int = 42,
            roles: List<Role> = emptyList(),
        ): UserProfile = UserProfile(
            user = User(id = id, name = name, email = "$id@example.com", roles = roles),
            stats = UserStats(
                userId = id,
                gamesPlayed = gamesPlayed,
                wins = wins,
                draws = 0,
                losses = gamesPlayed - wins,
                favoriteGames = emptyList(),
            ),
        )
    }
}
