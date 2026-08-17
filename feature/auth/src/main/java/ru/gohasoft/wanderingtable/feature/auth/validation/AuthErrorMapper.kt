package ru.gohasoft.wanderingtable.feature.auth.validation

import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.feature.auth.R

/** Maps a client-side [AuthValidator] failure to its inline form-field message. */
internal fun AppException.toFieldError(): TextResource = when (this) {
    is NameValidationException.Blank -> StringResource(R.string.auth_error_name_required)
    is NameValidationException.TooLong -> StringResource(R.string.auth_error_name_too_long)
    is EmailValidationException.InvalidFormat -> StringResource(R.string.auth_error_invalid_email)
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
 * error to an inline message. The [NetworkException.NotFound] branch is what the server answers today,
 * since the endpoint is not implemented yet — drop it once it is.
 */
internal fun AppException.toPasswordResetFieldError(): TextResource = when (this) {
    is NetworkException.NotFound -> StringResource(R.string.auth_error_password_reset_unavailable)
    is NetworkException.BadRequest -> StringResource(R.string.auth_error_bad_request)
    is NetworkException.NoInternet -> StringResource(R.string.auth_error_no_internet)
    else -> StringResource(R.string.auth_error_generic)
}
