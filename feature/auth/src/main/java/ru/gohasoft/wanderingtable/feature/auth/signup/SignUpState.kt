package ru.gohasoft.wanderingtable.feature.auth.signup

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: TextResource? = null,
    val emailError: TextResource? = null,
    val passwordError: TextResource? = null,
    val confirmPasswordError: TextResource? = null,
    val generalError: TextResource? = null,
    val isLoading: Boolean = false,
)
