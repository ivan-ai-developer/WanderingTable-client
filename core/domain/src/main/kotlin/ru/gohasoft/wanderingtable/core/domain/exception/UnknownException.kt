package ru.gohasoft.wanderingtable.core.domain.exception

class UnknownException(message: String = "something went wrong") : AppException(message)

fun Throwable.asAppException(): AppException =
    this as? AppException
        ?: UnknownException(message ?: cause?.message ?: "something went wrong")