package ru.gohasoft.wanderingtable.core.domain.model

import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.user.SkillLevel
import ru.gohasoft.wanderingtable.core.domain.model.user.User

data class OpponentRequest(
    val id: String,
    val author: User,
    val game: Game,
    val skillLevel: SkillLevel,
    val location: String,
    val createdAt: Long,
    val status: OpponentRequestStatus,
)

enum class OpponentRequestStatus {
    OPEN,
    MATCHED,
    CLOSED,
}
