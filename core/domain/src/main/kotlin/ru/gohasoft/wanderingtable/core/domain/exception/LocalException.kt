package ru.gohasoft.wanderingtable.core.domain.exception

sealed class LocalException(message: String) : AppException(message) {

    class DiskFull : LocalException("no free space on disk")

    class NotFound : LocalException("local data not found")

    class Unknown(message: String = "unknown local error") : LocalException(message)
}
