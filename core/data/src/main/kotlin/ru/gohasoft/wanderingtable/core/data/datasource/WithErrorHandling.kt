package ru.gohasoft.wanderingtable.core.data.datasource

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.domain.exception.UnknownException

/**
 * Runs [block] and rethrows any failure as a typed [AppException], so upper layers
 * (e.g. ResultFlow) only ever see app exceptions. Data sources wrap every call:
 * `suspend fun getNotes() = withErrorHandling { api.getNotes() }`.
 */
suspend inline fun <T> withErrorHandling(crossinline block: suspend () -> T): T {
    return try {
        block()
    } catch (exception: Throwable) {
        throw exception.asDataException()
    }
}

fun Throwable.asDataException(): Throwable = when (this) {
    is CancellationException -> this
    is AppException -> this
    is UnknownHostException, is ConnectException -> NetworkException.NoInternet()
    is SocketTimeoutException -> NetworkException.RequestTimeout()
    is kotlinx.serialization.SerializationException -> NetworkException.Serialization()
    is IOException -> NetworkException.Unknown(message ?: "unknown io error")
    else -> UnknownException(message ?: "something went wrong")
}
