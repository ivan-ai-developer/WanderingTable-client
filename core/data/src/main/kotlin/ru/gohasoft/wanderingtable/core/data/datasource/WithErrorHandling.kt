package ru.gohasoft.wanderingtable.core.data.datasource

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException
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
    is HttpException -> this.asNetworkException()
    is UnknownHostException, is ConnectException -> NetworkException.NoInternet()
    is SocketTimeoutException -> NetworkException.RequestTimeout()
    is kotlinx.serialization.SerializationException -> NetworkException.Serialization()
    is IOException -> NetworkException.Unknown(message ?: "unknown io error")
    else -> UnknownException(message ?: "something went wrong")
}

/** Maps a non-2xx HTTP response to the matching [NetworkException] subtype by status code. */
private fun HttpException.asNetworkException(): NetworkException = when (code()) {
    400 -> NetworkException.BadRequest()
    401 -> NetworkException.Unauthorized()
    403 -> NetworkException.Forbidden()
    404 -> NetworkException.NotFound()
    408 -> NetworkException.RequestTimeout()
    409 -> NetworkException.Conflict()
    413 -> NetworkException.PayloadTooLarge()
    429 -> NetworkException.TooManyRequests()
    in 500..599 -> NetworkException.ServerError()
    else -> NetworkException.Unknown("http ${code()}")
}
