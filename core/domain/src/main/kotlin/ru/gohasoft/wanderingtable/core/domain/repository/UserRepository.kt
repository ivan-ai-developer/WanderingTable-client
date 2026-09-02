package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile

interface UserRepository {

    /** The signed-in account together with its statistics. */
    fun getProfile(): Flow<Result<UserProfile>>

    /** The only mutable field on the account — email and avatar are not editable server-side. */
    fun updateName(name: String): Flow<Result<UserProfile>>

    /**
     * Finds a club member by email. Club-manager only; fails with
     * [ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.NotFound] when no
     * account uses that address.
     *
     * The returned [User] carries its current [Role]s, which [updateRoles] needs — see there.
     */
    fun findUserByEmail(email: String): Flow<Result<User>>

    /**
     * Club-manager only. **Replaces** the member's whole role set rather than adding to it, so
     * callers must pass the union of the current roles and the new one — read them from
     * [findUserByEmail] or [getProfile] first. `PLAYER` is re-added by the server regardless.
     *
     * A manager may target themselves; that is how they grant themselves `GAME_CREATOR` or
     * `NEWS_CREATOR`, since holding `CLUB_MANAGER` alone grants neither.
     */
    fun updateRoles(userId: String, roles: Set<Role>): Flow<Result<User>>
}
