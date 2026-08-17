package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository

internal class FakeAuthRepository : AuthRepository {

    var getSessionResult: Result<Session> = Result.Success(null)
    var logOutResult: Result<Unit> = Result.Success(Unit)
    var logOutCallCount = 0

    override fun signUp(name: String, email: String, password: String): Flow<Result<User>> =
        flowOf(Result.Success(null))

    override fun logIn(email: String, password: String): Flow<Result<Session>> =
        flowOf(Result.Success(null))

    override fun logOut(): Flow<Result<Unit>> {
        logOutCallCount++
        return flowOf(logOutResult)
    }

    override fun requestPasswordReset(email: String): Flow<Result<Unit>> =
        flowOf(Result.Success(Unit))

    override fun getSession(): Flow<Result<Session>> = flowOf(getSessionResult)
}
