package ru.gohasoft.wanderingtable.data.auth.fake

import ru.gohasoft.wanderingtable.data.auth.remote.api.SessionApi
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserDto
import ru.gohasoft.wanderingtable.data.auth.remote.dto.UserProfileDto

internal class FakeSessionApi : SessionApi {

    var meResult = UserDto(
        id = "user-1",
        name = "Alice",
        email = "alice@example.com",
        roles = listOf("PLAYER", "NEWS_CREATOR"),
    )
    var meError: Throwable? = null
    var meCallCount = 0

    override suspend fun me(): UserProfileDto {
        meCallCount++
        meError?.let { throw it }
        return UserProfileDto(meResult)
    }
}
