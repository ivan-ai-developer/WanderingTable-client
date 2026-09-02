package ru.gohasoft.wanderingtable.data.main.mapper

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import java.time.Instant
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventType
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.data.main.remote.dto.EventDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.GameDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.PageDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.UserProfileDto

class MainMappersTest {

    @Test
    fun `a page maps its content through`() {
        val page = PageDto(content = listOf(gameDto(), gameDto(id = "g2")), totalElements = 2)

        assertThat(page.toGames().map { it.id }).containsExactly("g1", "g2")
    }

    /** The server may add enum values without a contract change; one unknown row must not blank a list. */
    @Test
    fun `a list drops rows this build cannot model`() {
        val page = PageDto(content = listOf(eventDto(), eventDto(id = "e2", type = "MYSTERY_MODE")))

        assertThat(page.toGameEvents().map { it.id }).containsExactly("e1")
    }

    @Test
    fun `a single unmappable object is a parse failure, not a silent null`() {
        assertFailure { eventDto(type = "MYSTERY_MODE").toGameEvent() }
            .isInstanceOf(SerializationException::class)
    }

    @Test
    fun `an unparseable timestamp drops the row rather than crashing the list`() {
        val page = PageDto(content = listOf(eventDto(createdAt = "not-a-timestamp")))

        assertThat(page.toGameEvents()).containsExactly()
    }

    @Test
    fun `an event carries its schedule and seat maths`() {
        val event = eventDto(participantsCount = 1, maxParticipants = 4).toGameEvent()

        assertThat(event.type).isEqualTo(GameEventType.REGULAR_GAME)
        assertThat(event.status).isEqualTo(GameEventStatus.PLANNED)
        assertThat(event.startsAt).isEqualTo(Instant.parse("2026-08-01T18:00:00Z"))
        assertThat(event.seatsLeft).isEqualTo(3)
        assertThat(event.hasFreeSeats).isTrue()
    }

    @Test
    fun `a full event reports no free seats`() {
        val event = eventDto(participantsCount = 4, maxParticipants = 4).toGameEvent()

        assertThat(event.hasFreeSeats).isFalse()
        assertThat(event.seatsLeft).isEqualTo(0)
    }

    @Test
    fun `list responses leave participants unset`() {
        assertThat(eventDto().toGameEvent().participants).isNull()
    }

    @Test
    fun `a game keeps its result type and player bounds`() {
        val game = gameDto().toGame()

        assertThat(game.name).isEqualTo("Carcassonne")
        assertThat(game.resultType).isEqualTo(GameResultType.POINTS)
        assertThat(game.minPlayers).isEqualTo(2)
        assertThat(game.maxPlayers).isEqualTo(5)
    }

    @Test
    fun `unknown role names are dropped instead of failing the profile`() {
        val profile = UserProfileDto(
            user = UserDto(
                id = "u1",
                name = "Alex",
                email = "alex@example.com",
                roles = listOf("PLAYER", "TIME_LORD"),
            ),
        ).toUserProfile()

        assertThat(profile.user.roles.map { it.name }).containsExactly("PLAYER")
    }

    /** `stats` is optional on the wire; the profile screen still needs numbers to render. */
    @Test
    fun `a profile without stats reads as zeroes`() {
        val profile = UserProfileDto(
            user = UserDto(id = "u1", name = "Alex", email = "alex@example.com"),
        ).toUserProfile()

        assertThat(profile.stats.userId).isEqualTo("u1")
        assertThat(profile.stats.gamesPlayed).isEqualTo(0)
        assertThat(profile.stats.favoriteGames).containsExactly()
    }

    @Test
    fun `an unknown result type drops the game from the catalogue`() {
        val page = PageDto(content = listOf(gameDto(resultType = "VIBES")))

        assertThat(page.toGames()).containsExactly()
    }

    @Test
    fun `an unmappable game is a parse failure when fetched on its own`() {
        assertFailure { gameDto(resultType = "VIBES").toGame() }
            .isInstanceOf(SerializationException::class)
    }

    private fun gameDto(
        id: String = "g1",
        resultType: String = "POINTS",
    ): GameDto = GameDto(
        id = id,
        name = "Carcassonne",
        description = "Tiles and knights",
        minPlayers = 2,
        maxPlayers = 5,
        resultType = resultType,
    )

    private fun eventDto(
        id: String = "e1",
        type: String = "REGULAR_GAME",
        createdAt: String = "2026-07-30T10:15:30Z",
        participantsCount: Int = 1,
        maxParticipants: Int = 4,
    ): EventDto = EventDto(
        id = id,
        type = type,
        title = "Saturday game",
        description = "At the club",
        gameId = "g1",
        creatorId = "u1",
        status = "PLANNED",
        minParticipants = 2,
        maxParticipants = maxParticipants,
        participantsCount = participantsCount,
        createdAt = createdAt,
        startsAt = "2026-08-01T18:00:00Z",
        durationMinutes = 90,
    )
}
