package ru.gohasoft.wanderingtable.feature.auth.screen.login

internal sealed interface LoginEffect {
    data object SetStatusBarDefaultStyle : LoginEffect
}