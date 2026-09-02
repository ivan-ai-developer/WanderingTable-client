package ru.gohasoft.wanderingtable.data.main.remote.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.gohasoft.wanderingtable.data.main.remote.dto.NoteDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.PageDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UpsertNoteRequestDto

/**
 * Club news. `GET /notes` is public, but it is served by the authenticated client anyway — the
 * feed is only ever read from inside the app, and one client keeps the wiring simple.
 */
internal interface NotesApi {

    @GET("notes")
    suspend fun getNews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = DEFAULT_PAGE_SIZE,
    ): PageDto<NoteDto>

    /** Upsert by id; requires the `NEWS_CREATOR` role. */
    @POST("notes")
    suspend fun upsertNews(@Body body: UpsertNoteRequestDto): NoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNews(@Path("id") id: String)
}
