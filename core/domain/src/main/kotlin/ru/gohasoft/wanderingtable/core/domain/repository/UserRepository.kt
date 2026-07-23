package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.user.User

interface UserRepository {

    fun getUser(): Flow<Result<User>>

    fun updateEmail(string: String): Flow<Result<User>>

    fun updateName(string: String): Flow<Result<User>>

    fun updateAvatar(url: String): Flow<Result<User>>
}
