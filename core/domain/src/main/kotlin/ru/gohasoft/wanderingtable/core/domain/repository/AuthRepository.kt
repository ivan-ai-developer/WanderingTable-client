package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.model.user.User

interface AuthRepository {

    fun signUp(name: String, email: String, password: String): Flow<Result<User>>

    fun logIn(email: String, password: String): Flow<Result<Session>>

    fun logOut(): Flow<Result<Unit>>

    /**
     * Asks the server to send a password reset link. The endpoint does not exist server-side yet,
     * so today this always fails with [ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.NotFound].
     */
    fun requestPasswordReset(email: String): Flow<Result<Unit>>

    /** Emits the current user, or `Success(null)` when there is no active session. */
    fun getSession(): Flow<Result<Session>>
}
