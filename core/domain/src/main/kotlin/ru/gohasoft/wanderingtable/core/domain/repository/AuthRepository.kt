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
     * Asks the server to email a six-digit reset code. Succeeds for an unregistered address too —
     * the server answers 204 either way so the response can't be used to probe who has an account.
     */
    fun requestPasswordReset(email: String): Flow<Result<Unit>>

    /**
     * Exchanges the emailed code for a single-use reset token, the argument of [resetPassword].
     * The code is spent by the attempt whether or not it matched, so a failure means asking for a
     * new one. A wrong, expired or already used code surfaces as
     * [ru.gohasoft.wanderingtable.core.domain.exception.NetworkException.Unauthorized].
     */
    fun verifyPasswordResetCode(email: String, code: String): Flow<Result<String>>

    /**
     * Sets a new password. The server revokes every session of that account, so this never leaves
     * the user signed in — they go back to the login screen and sign in with the new password.
     */
    fun resetPassword(resetToken: String, newPassword: String): Flow<Result<Unit>>

    /** Emits the current user, or `Success(null)` when there is no active session. */
    fun getSession(): Flow<Result<Session>>
}
