package ru.gohasoft.wanderingtable.feature.auth.screen.signup

internal sealed interface SignUpEffect {
    data object SetStatusBarDefaultStyle : SignUpEffect
}