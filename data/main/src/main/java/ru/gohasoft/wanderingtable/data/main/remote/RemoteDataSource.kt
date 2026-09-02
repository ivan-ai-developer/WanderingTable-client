package ru.gohasoft.wanderingtable.data.main.remote

import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.data.datasource.withErrorHandling
import ru.gohasoft.wanderingtable.data.main.remote.api.DevicesApi
import ru.gohasoft.wanderingtable.data.main.remote.api.EventsApi
import ru.gohasoft.wanderingtable.data.main.remote.api.GamesApi
import ru.gohasoft.wanderingtable.data.main.remote.api.NotesApi
import ru.gohasoft.wanderingtable.data.main.remote.api.UsersApi
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateGameRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.CreateRegularGameRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.DeviceTokenRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.EventDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.GameDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.NoteDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.PageDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UpdateNameRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UpdateRolesRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UpsertNoteRequestDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserProfileDto

/**
 * The one place the main feature talks to the server. Every call goes through
 * [withErrorHandling], so callers above see `AppException`s rather than Retrofit or OkHttp types.
 */
internal class RemoteDataSource @Inject constructor(
    private val gamesApi: GamesApi,
    private val eventsApi: EventsApi,
    private val notesApi: NotesApi,
    private val usersApi: UsersApi,
    private val devicesApi: DevicesApi,
) {

    suspend fun getGames(query: String?): PageDto<GameDto> =
        withErrorHandling { gamesApi.getGames(name = query?.takeIf(String::isNotBlank)) }

    suspend fun getGame(gameId: String): GameDto = withErrorHandling { gamesApi.getGame(gameId) }

    suspend fun createGame(body: CreateGameRequestDto): GameDto =
        withErrorHandling { gamesApi.createGame(body) }

    suspend fun getEvents(status: String?): PageDto<EventDto> =
        withErrorHandling { eventsApi.getEvents(status = status) }

    suspend fun getEvent(eventId: String): EventDto = withErrorHandling { eventsApi.getEvent(eventId) }

    suspend fun getUserGames(userId: String): PageDto<EventDto> =
        withErrorHandling { eventsApi.getUserGames(userId) }

    suspend fun createRegularGame(body: CreateRegularGameRequestDto): EventDto =
        withErrorHandling { eventsApi.createRegularGame(body) }

    suspend fun joinEvent(eventId: String): EventDto = withErrorHandling { eventsApi.join(eventId) }

    suspend fun leaveEvent(eventId: String) = withErrorHandling { eventsApi.leave(eventId) }

    suspend fun cancelEvent(eventId: String): EventDto = withErrorHandling { eventsApi.cancel(eventId) }

    suspend fun getNews(): PageDto<NoteDto> = withErrorHandling { notesApi.getNews() }

    suspend fun upsertNews(newsId: String?, title: String, content: String): NoteDto =
        withErrorHandling { notesApi.upsertNews(UpsertNoteRequestDto(newsId, title, content)) }

    suspend fun deleteNews(newsId: String) = withErrorHandling { notesApi.deleteNews(newsId) }

    suspend fun getProfile(): UserProfileDto = withErrorHandling { usersApi.me() }

    suspend fun updateName(name: String) = withErrorHandling { usersApi.updateName(UpdateNameRequestDto(name)) }

    suspend fun findUserByEmail(email: String): UserDto =
        withErrorHandling { usersApi.findByEmail(email.trim()) }

    suspend fun updateRoles(userId: String, roles: List<String>): UserDto =
        withErrorHandling { usersApi.updateRoles(userId, UpdateRolesRequestDto(roles)) }

    /** The echoed device record carries nothing the app needs — only the round trip matters. */
    suspend fun registerDevice(token: String) {
        withErrorHandling { devicesApi.register(DeviceTokenRequestDto(token)) }
    }

    suspend fun unregisterDevice(token: String) =
        withErrorHandling { devicesApi.unregister(DeviceTokenRequestDto(token)) }
}
