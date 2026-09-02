package ru.gohasoft.wanderingtable.feature.main.clubadmin

import ru.gohasoft.wanderingtable.core.domain.model.user.Role

/** A member found by email, with the roles they hold right now. */
internal data class ClubMemberUi(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val roles: Set<Role>,
)
