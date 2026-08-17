package ru.gohasoft.wanderingtable.feature.auth.validation

import ru.gohasoft.wanderingtable.core.domain.exception.AppException

/**
 * Client-side pre-checks so obviously-bad input never reaches the network. [validatePassword]
 * mirrors the server's registration regex (`^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$`) exactly, and
 * [validateName] its "not blank, up to 64 characters" rule; login intentionally uses the looser
 * [validateLoginPassword] instead, since an existing account's password isn't guaranteed to
 * satisfy today's signup rules.
 */
internal object AuthValidator {

    private const val MAX_NAME_LENGTH = 64

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateName(value: String): NameValidationException? = when {
        value.isBlank() -> NameValidationException.Blank()
        value.length > MAX_NAME_LENGTH -> NameValidationException.TooLong()
        else -> null
    }

    fun validateEmail(value: String): EmailValidationException? {
        return if (EMAIL_REGEX.matches(value)) null else EmailValidationException.InvalidFormat()
    }

    fun validateLoginPassword(value: String): PasswordValidationException? {
        return if (value.isBlank()) PasswordValidationException.Blank() else null
    }

    fun validatePassword(value: String): PasswordValidationException? = when {
        value.isBlank() -> PasswordValidationException.Blank()
        value.length < 8 -> PasswordValidationException.TooShort()
        value.none { it.isLowerCase() } -> PasswordValidationException.MissingLowercase()
        value.none { it.isUpperCase() } -> PasswordValidationException.MissingUppercase()
        value.none { it.isDigit() } -> PasswordValidationException.MissingDigit()
        else -> null
    }
}

internal sealed class NameValidationException(message: String) : AppException(message) {
    class Blank : NameValidationException("name must not be blank")
    class TooLong : NameValidationException("name must be at most 64 characters")
}

internal sealed class EmailValidationException(message: String) : AppException(message) {
    class InvalidFormat : EmailValidationException("invalid email format")
}

internal sealed class PasswordValidationException(message: String) : AppException(message) {
    class Blank : PasswordValidationException("password must not be blank")
    class TooShort : PasswordValidationException("password must be at least 8 characters")
    class MissingLowercase : PasswordValidationException("password must contain a lowercase letter")
    class MissingUppercase : PasswordValidationException("password must contain an uppercase letter")
    class MissingDigit : PasswordValidationException("password must contain a digit")
}
