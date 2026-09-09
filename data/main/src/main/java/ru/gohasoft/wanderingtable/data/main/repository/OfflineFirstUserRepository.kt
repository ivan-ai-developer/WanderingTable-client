package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository
import ru.gohasoft.wanderingtable.data.main.local.CacheDataSource
import ru.gohasoft.wanderingtable.data.main.mapper.toUser
import ru.gohasoft.wanderingtable.data.main.mapper.toUserProfile
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource

/**
 * Statistics are recomputed server-side on every read, so the cached profile is a snapshot for the
 * offline case only — every fetch overwrites it, and the row is wiped on logout so the next
 * account on the device cannot see this one's numbers.
 */
internal class OfflineFirstUserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val cacheDataSource: CacheDataSource,
) : UserRepository {

    override fun getProfile(): Flow<Result<UserProfile>> = ResultFlow.offlineFirst(
        query = { cacheDataSource.getProfile() },
        fetch = { remoteDataSource.getProfile().toUserProfile() },
        saveFetchResult = { fetched, _ -> cacheDataSource.saveProfile(fetched) },
    )

    /** PATCH /users/me answers with the account only, so the fresh profile is read back. */
    override fun updateName(name: String): Flow<Result<UserProfile>> = ResultFlow.offlineFirst(
        query = { cacheDataSource.getProfile() },
        fetch = {
            remoteDataSource.updateName(name)
            remoteDataSource.getProfile().toUserProfile()
        },
        saveFetchResult = { fetched, _ -> cacheDataSource.saveProfile(fetched) },
    )

    /** Another member's account, looked up for role management — nothing of ours to cache. */
    override fun findUserByEmail(email: String): Flow<Result<User>> =
        ResultFlow.onlineOnly { remoteDataSource.findUserByEmail(email).toUser() }

    override fun updateRoles(userId: String, roles: Set<Role>): Flow<Result<User>> =
        ResultFlow.onlineOnly {
            remoteDataSource.updateRoles(userId, roles.map(Role::name)).toUser()
        }
}
