package ru.gohasoft.wanderingtable.feature.auth.screen.newpassword

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class NewPasswordState(
    val password: String = "",
    val confirmPassword: String = "",
    val passwordError: TextResource? = null,
    val confirmPasswordError: TextResource? = null,
    val generalError: TextResource? = null,
    val isLoading: Boolean = false,
)
