package ru.gohasoft.wanderingtable.data.auth.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.gohasoft.wanderingtable.data.auth.remote.dto.AuthTokensResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.ForgotPasswordRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.LoginRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RefreshTokenRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RegisterRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto

/** The public `auth` endpoints. Served by the plain client — none of them takes a token. */
internal interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): UserDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthTokensResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequestDto): AuthTokensResponseDto

    /** Responds 204 and is idempotent — logging out twice is not an error. */
    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshTokenRequestDto)

    /**
     * Not implemented on the server yet, so today this answers 404 and the screen says so. The
     * client is written against the intended contract: 204 on success, 400 on a malformed email.
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto)
}
