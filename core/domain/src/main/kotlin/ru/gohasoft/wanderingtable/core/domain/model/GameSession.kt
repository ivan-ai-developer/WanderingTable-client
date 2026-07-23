package ru.gohasoft.wanderingtable.core.domain.model

import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import java.time.LocalDateTime

data class GameSession(
    val id: String,
    val host: User,
    val game: Game,
    val dateTime: LocalDateTime,
    val location: String,
    val maxPlayers: Int,
    val participants: List<User>,
    val status: GameSessionStatus,
)

enum class GameSessionStatus {
    UPCOMING,
    IN_PROGRESS,
    FINISHED,
    CANCELLED,
}
