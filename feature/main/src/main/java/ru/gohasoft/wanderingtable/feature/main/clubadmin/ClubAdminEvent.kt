package ru.gohasoft.wanderingtable.feature.main.clubadmin

import ru.gohasoft.wanderingtable.core.domain.model.user.Role

internal sealed interface ClubAdminEvent {
    data object OnBackClick : ClubAdminEvent
    data class OnMyRoleChanged(val role: Role, val granted: Boolean) : ClubAdminEvent
    data class OnEmailChanged(val email: String) : ClubAdminEvent
    data object OnSearchClick : ClubAdminEvent
    data object OnClearMemberClick : ClubAdminEvent
    data class OnMemberRoleChanged(val role: Role, val granted: Boolean) : ClubAdminEvent
    data object OnRetryClick : ClubAdminEvent
}
