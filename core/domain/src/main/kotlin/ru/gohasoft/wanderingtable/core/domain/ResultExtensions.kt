package ru.gohasoft.wanderingtable.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.UnknownException

fun <T, R> Flow<Result<T>>.mapData(
    map: (T?) -> R?,
): Flow<Result<R>> = map { result ->
    when (result) {
        is Result.Success -> Result.Success(map(result.data))
        is Result.Error -> Result.Error(result.error, map(result.data))
        is Result.Loading -> Result.Loading(map(result.data))
    }
}

fun <T> Flow<Result<T>>.onSuccess(
    action: suspend (T?) -> Unit,
): Flow<Result<T>> = onEach { result ->
    if (result is Result.Success) action(result.data)
}

fun <T> Flow<Result<T>>.onFailure(
    action: suspend (AppException) -> Unit,
): Flow<Result<T>> = onEach { result ->
    if (result is Result.Error) action(result.error)
}

fun <T> Flow<Result<T>>.asEmptyResult(): Flow<Result<Unit>> = mapData { }

suspend fun <T> Flow<Result<T>>.firstSuccessOrErrorResult(): Result<T>? =
    firstOrNull { result -> result.isSuccessOrError }

suspend fun <T> Flow<Result<T>>.firstSuccessOrErrorResult(
    action: (Result<T>) -> Unit
) = firstOrNull { result ->
    action(result)
    result.isSuccessOrError
}

suspend fun <T> Flow<Result<T>>.firstSuccessOrErrorData(): T? =
    firstSuccessOrErrorResult()?.data

suspend fun <T> Flow<Result<T>>.firstSuccessOrErrorData(
    action: (T?) -> Unit
) = firstSuccessOrErrorResult { result ->
    action(result.data)
}?.data

fun <T> Result<T>?.orUnknownErrorResult(
    message: String = "something went wrong"
) = this ?: Result.Error(UnknownException(message))
