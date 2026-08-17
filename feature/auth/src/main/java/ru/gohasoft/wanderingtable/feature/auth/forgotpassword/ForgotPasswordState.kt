package ru.gohasoft.wanderingtable.feature.auth.forgotpassword

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class ForgotPasswordState(
    val email: String = "",
    val emailError: TextResource? = null,
    val generalError: TextResource? = null,
    val isLoading: Boolean = false,
    /** Once true the form is replaced by a confirmation, so the request can't be fired twice. */
    val isLinkSent: Boolean = false,
)
