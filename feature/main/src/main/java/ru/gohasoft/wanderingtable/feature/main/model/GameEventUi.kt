package ru.gohasoft.wanderingtable.feature.main.model

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

/**
 * A club play as a card renders it.
 *
 * [skillLabel] is a constant "Any level": the design badges every request with a skill, but the
 * server models none for a regular game, so nothing is ever posted or read back for it.
 * [hostLine] can only say whether the play is yours — there is no endpoint that resolves another
 * member's name from their id.
 */
internal data class GameEventUi(
    val id: String,
    val title: String,
    val hostLine: TextResource,
    val meta: TextResource,
    val skillLabel: TextResource,
    val isMine: Boolean,
    val isJoined: Boolean,
    val canJoin: Boolean,
)
