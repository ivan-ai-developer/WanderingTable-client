package ru.gohasoft.wanderingtable.data.auth.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.gohasoft.wanderingtable.data.auth.remote.dto.AuthTokensResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.ForgotPasswordRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.LoginRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RefreshTokenRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.RegisterRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.ResetPasswordRequestDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.ResetTokenResponseDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.VerifyResetCodeRequestDto

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
     * Emails a six-digit code. Answers 204 for an unregistered address too, so a caller cannot
     * tell from the response whether the account exists.
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto)

    /** 401 when the code is wrong, expired or already spent — the server does not say which. */
    @POST("auth/verify-reset-code")
    suspend fun verifyResetCode(@Body body: VerifyResetCodeRequestDto): ResetTokenResponseDto

    /** Responds 204. Every session of the account is revoked server-side. */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto)
}
