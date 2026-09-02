package ru.gohasoft.wanderingtable.feature.main.gamedetail

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

/**
 * Everything Game Detail shows.
 *
 * [participantInitials] is a list of placeholders rather than names: the event carries participant
 * ids, and no endpoint turns an id into a display name.
 */
internal data class GameDetailState(
    val isLoading: Boolean = true,
    val title: String = "",
    val hostLine: TextResource? = null,
    val hostInitials: String = "?",
    val skillLabel: TextResource? = null,
    val dateTimeLabel: String = "",
    val locationLabel: TextResource? = null,
    val playersLabel: TextResource? = null,
    val participantInitials: List<String> = emptyList(),
    val note: String? = null,
    val action: GameDetailAction = GameDetailAction.NONE,
    val isActionInProgress: Boolean = false,
    val error: TextResource? = null,
) {
    val isLoaded: Boolean get() = !isLoading && title.isNotEmpty()
}
