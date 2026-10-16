package ru.gohasoft.wanderingtable.feature.auth.mapper

import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.validation.EmailValidationException
import ru.gohasoft.wanderingtable.feature.auth.validation.NameValidationException
import ru.gohasoft.wanderingtable.feature.auth.validation.PasswordValidationException
import ru.gohasoft.wanderingtable.feature.auth.validation.ResetCodeValidationException

/** Maps a client-side [ru.gohasoft.wanderingtable.feature.auth.validation.AuthValidator] failure to its inline form-field message. */
internal fun AppException.toFieldError(): TextResource = when (this) {
    is NameValidationException.Blank -> StringResource(R.string.auth_error_name_required)
    is NameValidationException.TooLong -> StringResource(R.string.auth_error_name_too_long)
    is EmailValidationException.InvalidFormat -> StringResource(R.string.auth_error_invalid_email)
    is ResetCodeValidationException.InvalidFormat -> StringResource(R.string.auth_error_reset_code_required)
    is PasswordValidationException.Blank -> StringResource(R.string.auth_error_password_required)
    is PasswordValidationException.TooShort -> StringResource(R.string.auth_error_password_too_short)
    is PasswordValidationException.MissingLowercase -> StringResource(R.string.auth_error_password_missing_lowercase)
    is PasswordValidationException.MissingUppercase -> StringResource(R.string.auth_error_password_missing_uppercase)
    is PasswordValidationException.MissingDigit -> StringResource(R.string.auth_error_password_missing_digit)
    else -> StringResource(R.string.auth_error_generic)
}

/** Maps a failed [ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository.logIn] error to an inline message. */
internal fun AppException.toLoginFieldError(): TextResource = when (this) {
    is NetworkException.Unauthorized -> StringResource(R.string.auth_error_login_invalid_credentials)
    is NetworkException.BadRequest -> StringResource(R.string.auth_error_bad_request)
    is NetworkException.NoInternet -> StringResource(R.string.auth_error_no_internet)
    else -> StringResource(R.string.auth_error_generic)
}

/** Maps a failed [ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository.signUp] error to an inline message. */
internal fun AppException.toSignUpFieldError(): TextResource = when (this) {
    is NetworkException.Conflict -> StringResource(R.string.auth_error_signup_email_taken)
    is NetworkException.BadRequest -> StringResource(R.string.auth_error_bad_request)
    is NetworkException.NoInternet -> StringResource(R.string.auth_error_no_internet)
    else -> StringResource(R.string.auth_error_generic)
}

/**
 * Maps a failed [ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository.requestPasswordReset]
 * error to an inline message. There is no "unknown email" branch on purpose: the server answers 204
 * for an unregistered address, so this screen can never learn — or reveal — that an account is missing.
 */
internal fun AppException.toPasswordResetFieldError(): TextResource = when (this) {
    is NetworkException.TooManyRequests -> StringResource(R.string.auth_error_too_many_attempts)
    is NetworkException.BadRequest -> StringResource(R.string.auth_error_bad_request)
    is NetworkException.NoInternet -> StringResource(R.string.auth_error_no_internet)
    else -> StringResource(R.string.auth_error_generic)
}

/**
 * Maps a failed [ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository.verifyPasswordResetCode]
 * error to an inline message. The 401 covers wrong, expired and already-spent codes alike — the server
 * does not distinguish them, and the code is spent by the attempt either way, so the message says to
 * ask for a new one.
 */
internal fun AppException.toResetCodeFieldError(): TextResource = when (this) {
    is NetworkException.Unauthorized -> StringResource(R.string.auth_error_reset_code_rejected)
    is NetworkException.TooManyRequests -> StringResource(R.string.auth_error_too_many_attempts)
    is NetworkException.BadRequest -> StringResource(R.string.auth_error_reset_code_required)
    is NetworkException.NoInternet -> StringResource(R.string.auth_error_no_internet)
    else -> StringResource(R.string.auth_error_generic)
}

/**
 * Maps a failed [ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository.resetPassword]
 * error to an inline message. A 401 here means the reset token expired or was already used, which
 * only the whole flow can fix; a 400 means the password itself was rejected by the server's rules.
 */
internal fun AppException.toNewPasswordFieldError(): TextResource = when (this) {
    is NetworkException.Unauthorized -> StringResource(R.string.auth_error_reset_request_expired)
    is NetworkException.BadRequest -> StringResource(R.string.auth_error_password_too_short)
    is NetworkException.NoInternet -> StringResource(R.string.auth_error_no_internet)
    else -> StringResource(R.string.auth_error_generic)
}
