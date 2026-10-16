package ru.gohasoft.wanderingtable.feature.auth.screen.resetcode

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class EnterResetCodeState(
    val email: String = "",
    val code: String = "",
    val codeError: TextResource? = null,
    val generalError: TextResource? = null,
    val isLoading: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    /** Shown after a successful resend so the user knows the older code is now dead. */
    val isCodeResent: Boolean = false,
)
