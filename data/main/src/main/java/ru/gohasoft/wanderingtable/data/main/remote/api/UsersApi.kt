package ru.gohasoft.wanderingtable.data.main.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import ru.gohasoft.wanderingtable.data.main.remote.dto.UpdateNameRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UpdateRolesRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserProfileDto

internal interface UsersApi {

    @GET("users/me")
    suspend fun me(): UserProfileDto

    @PATCH("users/me")
    suspend fun updateName(@Body body: UpdateNameRequestDto): UserDto

    /** Club-manager only. 404 when no account uses that address. */
    @GET("users")
    suspend fun findByEmail(@Query("email") email: String): UserDto

    /** Club-manager only. The body replaces the member's whole role set. */
    @PATCH("users/{id}/roles")
    suspend fun updateRoles(
        @Path("id") userId: String,
        @Body body: UpdateRolesRequestDto,
    ): UserDto
}
