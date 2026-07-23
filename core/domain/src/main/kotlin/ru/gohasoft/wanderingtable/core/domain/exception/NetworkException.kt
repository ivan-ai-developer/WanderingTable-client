package ru.gohasoft.wanderingtable.core.domain.exception

sealed class NetworkException(message: String) : AppException(message) {

    class NoInternet : NetworkException("no internet connection")

    class BadRequest : NetworkException("bad request")

    class RequestTimeout : NetworkException("request timeout")

    class Unauthorized : NetworkException("unauthorized")

    class Forbidden : NetworkException("forbidden")

    class NotFound : NetworkException("not found")

    class Conflict : NetworkException("conflict")

    class PayloadTooLarge : NetworkException("payload too large")

    class TooManyRequests : NetworkException("too many requests")

    class ServerError : NetworkException("server error")

    class ServiceUnavailable : NetworkException("service unavailable")

    class Serialization : NetworkException("serialization error")

    class Unknown(message: String = "unknown network error") : NetworkException(message)
}
