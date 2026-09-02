package ru.gohasoft.wanderingtable.feature.main.creategame

import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

/**
 * The "Add a game" form.
 *
 * Every field here has a server field behind it, unlike Create Request — this is the one place the
 * catalogue is written from.
 */
internal data class CreateGameState(
    val name: String = "",
    val description: String = "",
    val minPlayers: Int = DEFAULT_MIN_PLAYERS,
    val maxPlayers: Int = DEFAULT_MAX_PLAYERS,
    val resultType: GameResultType = GameResultType.POINTS,
    val isSubmitting: Boolean = false,
    val formError: TextResource? = null,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !isSubmitting

    internal companion object {
        const val DEFAULT_MIN_PLAYERS = 2
        const val DEFAULT_MAX_PLAYERS = 4

        /** One player is a solo game; the club's largest boxes sit well under this. */
        const val MIN_PLAYERS_LIMIT = 1
        const val MAX_PLAYERS_LIMIT = 12
    }
}
