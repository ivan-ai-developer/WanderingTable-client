package ru.gohasoft.wanderingtable.core.domain

import ru.gohasoft.wanderingtable.core.domain.exception.AppException

sealed class Result<out T> {

    open val data: T? = null

    open val isSuccess = false
    open val isError = false
    open val isLoading = false

    data class Success<out T>(override val data: T?) : Result<T>() {
        override val isSuccess: Boolean = true
    }

    data class Error<out T>(val error: AppException, override val data: T? = null) : Result<T>() {
        override val isError: Boolean = true
    }

    data class Loading<out T>(override val data: T? = null) : Result<T>() {
        override val isLoading: Boolean = true
    }
}

val <T> Result<T>.isSuccessOrError: Boolean
    get() = isSuccess || isError