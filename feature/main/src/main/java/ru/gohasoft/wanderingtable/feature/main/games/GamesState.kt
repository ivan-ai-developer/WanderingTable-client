package ru.gohasoft.wanderingtable.feature.main.games

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.main.model.GameEventUi

internal data class GamesState(
    val isLoading: Boolean = true,
    val filter: GamesFilter = GamesFilter.ALL,
    /** Every planned club play, unfiltered; the chips narrow it in [visibleGames]. */
    val games: List<GameEventUi> = emptyList(),
    val joiningEventId: String? = null,
    val error: TextResource? = null,
) {

    val visibleGames: List<GameEventUi>
        get() = when (filter) {
            GamesFilter.ALL -> games
            GamesFilter.OPEN_REQUESTS -> games.filter { it.canJoin }
            GamesFilter.MY_GAMES -> games.filter { it.isMine || it.isJoined }
        }
}
