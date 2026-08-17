package ru.gohasoft.wanderingtable.feature.auth.login

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: TextResource? = null,
    val passwordError: TextResource? = null,
    val generalError: TextResource? = null,
    val isLoading: Boolean = false,
)
