package ru.gohasoft.wanderingtable.feature.main.clubadmin

import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class ClubAdminState(
    val isLoading: Boolean = true,
    val myUserId: String = "",
    val myRoles: Set<Role> = emptySet(),
    val email: String = "",
    val isSearching: Boolean = false,
    val member: ClubMemberUi? = null,
    val searchError: TextResource? = null,
    /** The role whose switch is mid-request, so only that one shows as busy. */
    val savingRole: Role? = null,
    val error: TextResource? = null,
) {
    val canSearch: Boolean get() = email.isNotBlank() && !isSearching

    internal companion object {
        /**
         * `PLAYER` is missing on purpose: the server re-adds it to every update, so offering a
         * switch that cannot turn off would only mislead.
         */
        val GRANTABLE_ROLES = listOf(
            Role.GAME_CREATOR,
            Role.NEWS_CREATOR,
            Role.TOURNAMENT_CREATOR,
            Role.CLUB_MANAGER,
        )
    }
}
