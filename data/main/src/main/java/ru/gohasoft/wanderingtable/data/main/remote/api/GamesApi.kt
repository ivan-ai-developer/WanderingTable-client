package ru.gohasoft.wanderingtable.data.main.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateGameRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.GameDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.PageDto

/** The game catalogue. Reading it needs a token, so this rides the authenticated client. */
internal interface GamesApi {

    @GET("games")
    suspend fun getGames(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = DEFAULT_PAGE_SIZE,
    ): PageDto<GameDto>

    @GET("games/{id}")
    suspend fun getGame(@Path("id") id: String): GameDto

    /** Requires the `GAME_CREATOR` role; 409 when a game with that name already exists. */
    @POST("games")
    suspend fun createGame(@Body body: CreateGameRequestDto): GameDto
}
