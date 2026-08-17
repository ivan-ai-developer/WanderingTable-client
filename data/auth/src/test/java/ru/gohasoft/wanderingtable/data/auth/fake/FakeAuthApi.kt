package ru.gohasoft.wanderingtable.data.auth.fake

import ru.gohasoft.wanderingtable.data.auth.remote.api.AuthApi
import ru.gohasoft.wanderingtable.data.auth.remote.dto.AuthTokensResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.ForgotPasswordRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.LoginRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RefreshTokenRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RegisterRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto

internal class FakeAuthApi : AuthApi {

    var registerResult = UserDto(
        id = "user-1",
        name = "Alice",
        email = "alice@example.com",
        roles = listOf("PLAYER"),
    )
    var loginResult = AuthTokensResponseDto("access-token", "refresh-token")
    var refreshResult = AuthTokensResponseDto("new-access-token", "new-refresh-token")

    var registerError: Throwable? = null
    var loginError: Throwable? = null
    var logoutError: Throwable? = null
    var forgotPasswordError: Throwable? = null

    var registerCallCount = 0
    var refreshCallCount = 0
    var forgotPasswordCallCount = 0
    var lastForgotPasswordEmail: String? = null
    var loggedOutTokens = mutableListOf<String>()

    override suspend fun register(body: RegisterRequestDto): UserDto {
        registerCallCount++
        registerError?.let { throw it }
        return registerResult.copy(name = body.name, email = body.email)
    }

    override suspend fun login(body: LoginRequestDto): AuthTokensResponseDto {
        loginError?.let { throw it }
        return loginResult
    }

    override suspend fun refresh(body: RefreshTokenRequestDto): AuthTokensResponseDto {
        refreshCallCount++
        return refreshResult
    }

    override suspend fun logout(body: RefreshTokenRequestDto) {
        logoutError?.let { throw it }
        loggedOutTokens += body.refreshToken
    }

    override suspend fun forgotPassword(body: ForgotPasswordRequestDto) {
        forgotPasswordCallCount++
        lastForgotPasswordEmail = body.email
        forgotPasswordError?.let { throw it }
    }
}
