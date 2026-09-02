package ru.gohasoft.wanderingtable.core.domain.model.user

/** What `GET /users/me` returns: the account and its statistics in one shot. */
data class UserProfile(
    val user: User,
    val stats: UserStats,
)
