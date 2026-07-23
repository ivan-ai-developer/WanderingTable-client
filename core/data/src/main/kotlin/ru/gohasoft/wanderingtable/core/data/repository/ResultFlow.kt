package ru.gohasoft.wanderingtable.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.asAppException

object ResultFlow {

    fun <ResultType> offlineOnly(
        query: Flow<ResultType>,
    ): Flow<Result<ResultType>> = query
        .flowOn(Dispatchers.IO)
        .map { Result.Success(it) as Result<ResultType> }
        .catch { error ->
            emit(Result.Error(error.asAppException()))
        }

    inline fun <ResultType> offlineOnly(
        crossinline query: suspend () -> ResultType?,
    ): Flow<Result<ResultType>> = flow<Result<ResultType>> {
        emit(Result.Success(query()))
    }
        .flowOn(Dispatchers.IO)
        .catch { error ->
            emit(Result.Error(error.asAppException()))
        }

    inline fun <ResultType> onlineOnly(
        crossinline fetch: suspend () -> ResultType?,
    ): Flow<Result<ResultType>> = flow<Result<ResultType>> {
        emit(Result.Success(fetch()))
    }
        .flowOn(Dispatchers.IO)
        .onStart {
            emit(Result.Loading())
        }
        .catch { error ->
            emit(Result.Error(error.asAppException()))
        }

    inline fun <ResultType, RequestType> offlineFirst(
        crossinline query: () -> Flow<ResultType>,
        crossinline fetch: suspend () -> RequestType,
        crossinline saveFetchResult: suspend (RequestType, ResultType) -> Unit,
        crossinline shouldFetch: (ResultType) -> Boolean = { true },
        crossinline onFetchSuccess: () -> Unit = {},
        crossinline onFetchFailed: (AppException) -> Unit = {},
    ): Flow<Result<ResultType>> = channelFlow {
        val data = query().first()
        if (shouldFetch(data)) {
            val loading = launch {
                query().collect { cache -> send(Result.Loading(cache)) }
            }
            runCatching {
                saveFetchResult(fetch(), data)
                onFetchSuccess()
                loading.cancel()
                query().collect { cache -> send(Result.Success(cache)) }
            }.getOrElse { error ->
                onFetchFailed(error.asAppException())
                loading.cancel()
                query().collect { cache -> send(Result.Error(error.asAppException(), cache)) }
            }
        } else {
            query().collect { cache -> send(Result.Success(cache)) }
        }
    }.flowOn(Dispatchers.IO)
}