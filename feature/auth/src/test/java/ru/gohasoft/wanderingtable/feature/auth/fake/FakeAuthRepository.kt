package ru.gohasoft.wanderingtable.feature.auth.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository

internal class FakeAuthRepository : AuthRepository {

    var logInResult: Result<Session> =
        Result.Success(Session(User(id = "1", name = "", email = "test@example.com")))
    var signUpResult: Result<User> =
        Result.Success(User(id = "1", name = "", email = "test@example.com"))
    var getSessionResult: Result<Session> = Result.Success(null)
    var requestPasswordResetResult: Result<Unit> = Result.Success(Unit)
    var verifyPasswordResetCodeResult: Result<String> = Result.Success("reset-token")
    var resetPasswordResult: Result<Unit> = Result.Success(Unit)

    var logInCallCount = 0
    var signUpCallCount = 0
    var requestPasswordResetCallCount = 0
    var lastSignUpName: String? = null
    var verifyPasswordResetCodeCallCount = 0
    var resetPasswordCallCount = 0
    var lastPasswordResetEmail: String? = null
    var lastVerifiedCode: String? = null
    var lastResetToken: String? = null
    var lastNewPassword: String? = null

    override fun signUp(name: String, email: String, password: String): Flow<Result<User>> {
        signUpCallCount++
        lastSignUpName = name
        return flowOf(signUpResult)
    }

    override fun logIn(email: String, password: String): Flow<Result<Session>> {
        logInCallCount++
        return flowOf(logInResult)
    }

    override fun logOut(): Flow<Result<Unit>> = flowOf(Result.Success(Unit))

    override fun requestPasswordReset(email: String): Flow<Result<Unit>> {
        requestPasswordResetCallCount++
        lastPasswordResetEmail = email
        return flowOf(requestPasswordResetResult)
    }

    override fun verifyPasswordResetCode(email: String, code: String): Flow<Result<String>> {
        verifyPasswordResetCodeCallCount++
        lastPasswordResetEmail = email
        lastVerifiedCode = code
        return flowOf(verifyPasswordResetCodeResult)
    }

    override fun resetPassword(resetToken: String, newPassword: String): Flow<Result<Unit>> {
        resetPasswordCallCount++
        lastResetToken = resetToken
        lastNewPassword = newPassword
        return flowOf(resetPasswordResult)
    }

    override fun getSession(): Flow<Result<Session>> = flowOf(getSessionResult)
}
