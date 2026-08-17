package ru.gohasoft.wanderingtable.feature.main.welcome

internal sealed interface WelcomeEvent {
    data object OnLogoutClick : WelcomeEvent
}
