package ru.gohasoft.wanderingtable.data.auth.remote

import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.data.datasource.withErrorHandling
import ru.gohasoft.wanderingtable.data.auth.remote.api.AuthApi
import ru.gohasoft.wanderingtable.data.auth.remote.api.SessionApi
import ru.gohasoft.wanderingtable.data.auth.remote.dto.AuthTokensResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.ForgotPasswordRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.LoginRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RefreshTokenRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RegisterRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto

internal class RemoteDataSource @Inject constructor(
    private val authApi: AuthApi,
    private val sessionApi: SessionApi,
) {

    suspend fun register(name: String, email: String, password: String): UserDto =
        withErrorHandling { authApi.register(RegisterRequestDto(name, email, password)) }

    suspend fun login(email: String, password: String): AuthTokensResponseDto =
        withErrorHandling { authApi.login(LoginRequestDto(email, password)) }

    suspend fun refresh(refreshToken: String): AuthTokensResponseDto =
        withErrorHandling { authApi.refresh(RefreshTokenRequestDto(refreshToken)) }

    suspend fun logout(refreshToken: String) =
        withErrorHandling { authApi.logout(RefreshTokenRequestDto(refreshToken)) }

    suspend fun forgotPassword(email: String) =
        withErrorHandling { authApi.forgotPassword(ForgotPasswordRequestDto(email)) }

    suspend fun getUser(): UserDto = withErrorHandling { sessionApi.me().user }
}
