package ru.gohasoft.wanderingtable.feature.main.welcome

internal data class WelcomeState(
    val userEmail: String = "",
    val isLoggingOut: Boolean = false,
)
