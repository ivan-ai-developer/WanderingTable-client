package ru.gohasoft.wanderingtable.data.auth.remote.api

import retrofit2.http.GET
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserProfileDto

/**
 * The one protected endpoint session handling needs: it is the only source of the signed-in
 * user's id, display name and roles. Served by the authenticated client, so a 401 here triggers
 * a transparent token refresh.
 */
internal interface SessionApi {

    @GET("users/me")
    suspend fun me(): UserProfileDto
}
