package ru.gohasoft.wanderingtable.core.domain.exception

import java.io.IOException

abstract class AppException(message: String = "unknown") : IOException("Error: $message")