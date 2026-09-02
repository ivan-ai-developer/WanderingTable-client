package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.model.user.UserProfile
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository
import ru.gohasoft.wanderingtable.data.main.mapper.toUser
import ru.gohasoft.wanderingtable.data.main.mapper.toUserProfile
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource

/** Statistics are recomputed server-side on every read, so the profile is never cached. */
internal class NetworkUserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : UserRepository {

    override fun getProfile(): Flow<Result<UserProfile>> =
        ResultFlow.onlineOnly { remoteDataSource.getProfile().toUserProfile() }

    /** PATCH /users/me answers with the account only, so the fresh profile is read back. */
    override fun updateName(name: String): Flow<Result<UserProfile>> = ResultFlow.onlineOnly {
        remoteDataSource.updateName(name)
        remoteDataSource.getProfile().toUserProfile()
    }

    override fun findUserByEmail(email: String): Flow<Result<User>> =
        ResultFlow.onlineOnly { remoteDataSource.findUserByEmail(email).toUser() }

    override fun updateRoles(userId: String, roles: Set<Role>): Flow<Result<User>> =
        ResultFlow.onlineOnly {
            remoteDataSource.updateRoles(userId, roles.map(Role::name)).toUser()
        }
}
