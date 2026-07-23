package ru.gohasoft.wanderingtable.core.domain.model.user

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val roles: List<Role> = emptyList()
)
