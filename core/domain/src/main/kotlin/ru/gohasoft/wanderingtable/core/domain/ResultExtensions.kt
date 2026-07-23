package ru.gohasoft.wanderingtable.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.gohasoft.wanderingtable.core.domain.exception.AppException

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
