package ru.gohasoft.wanderingtable.data.main.remote.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateRegularGameRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.EventDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.PageDto

/**
 * The club schedule. Tournaments live under `/events/tournaments`, but they share this endpoint's
 * join / leave / cancel operations, so those are modelled once here.
 */
internal interface EventsApi {

    @GET("events")
    suspend fun getEvents(
        @Query("status") status: String? = null,
        @Query("gameId") gameId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = DEFAULT_PAGE_SIZE,
    ): PageDto<EventDto>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventDto

    /** Plays the user took part in, both club and tournament ones, newest first. */
    @GET("users/{id}/games")
    suspend fun getUserGames(
        @Path("id") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = DEFAULT_PAGE_SIZE,
    ): PageDto<EventDto>

    @POST("events/regular-games")
    suspend fun createRegularGame(@Body body: CreateRegularGameRequestDto): EventDto

    /** 409 when the event is full, already started, or the caller is already a participant. */
    @POST("events/{id}/join")
    suspend fun join(@Path("id") id: String): EventDto

    @DELETE("events/{id}/leave")
    suspend fun leave(@Path("id") id: String)

    /** Creator or club manager only. The event stays readable as `CANCELLED`. */
    @DELETE("events/{id}")
    suspend fun cancel(@Path("id") id: String): EventDto
}
