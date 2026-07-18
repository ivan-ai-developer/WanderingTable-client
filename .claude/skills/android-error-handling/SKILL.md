---
name: android-error-handling
description: |
  Generic Result wrapper, error types, and extension helpers for Android, Result, map, onSuccess, onFailure. Use this skill whenever defining error types, creating a Result wrapper, handling success/failure/loading flows, mapping errors, or working with typed errors anywhere in the app (not just data layer — also validation, auth, domain logic). Trigger on phrases like "Result wrapper", "error handling", "Result Flow", "onSuccess", "onFailure", "map result", "error type", "validation error", or "typed errors".
---

# Android

## Result Wrapper (`core:domain`)

A generic, typed Result that works across all layers — data, domain, presentation, validation, anywhere a function can succeed  or loading or fail with a typed error.
Result is a sealed class with three subclasses: Success, Error, and Loading. All subclasses have data from cache.

---

## Extension Helpers (`core:domain`)

These live alongside the `Result` definition:

```kotlin
fun <T, R> Flow<Result<T>>.mapData(
    map: (T) -> R
): Flow<Result<R>> = map {
    return when (this) {
        is Result.Error -> Result.Error(error, map(this.data))
        is Result.Success -> Result.Success(map(this.data))
        is Result.Loading -> Result.Loading(map(this.data))
    }
}

fun <T> Flow<Result<T>>.onSuccess(
    action: (T) -> Unit
): Flow<Result<T>> = onEach { result ->
    if (result is Result.Success) action(result.data)
}

inline fun <T> Flow<Result<T>>.onFailure(
    action: (AppException) -> Unit
): Flow<Result<T>> = onEach { result ->
    if (result is Result.Success) action(result.data)
}

fun Flow<Result<T>>.asEmptyResult(): Flow<Result<Unit>> {
    return mapData { }
}
```

All helpers return `Flow<Result>` so they can be chained:
```kotlin
repository.saveNote(note)
    .onSuccess { /* update UI */ }
    .onFailure { /* show error */ }
    .asEmptyResult()
```

---

## Shared Exception Types (`core:domain`)

abstract class AppException is root of all app exceptions. All exceptions have to extend it.  All exceptions have to have message, in AppException this message log if it is debug.

### DataError

Where are network errors BAD_REQUEST, REQUEST_TIMEOUT, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, CONFLICT, TOO_MANY_REQUESTS, NO_INTERNET, PAYLOAD_TOO_LARGE, SERVER_ERROR, SERVICE_UNAVAILABLE, SERIALIZATION, UNKNOWN and local errors DISK_FULL, NOT_FOUND, UNKNOWN from specific frameworks (like Retrofit or Room). They cast to specific AppException in withErrorHandling fun.

### Feature-Specific Errors

Features define their own error types by implementing `AppException`:

```kotlin
internal sealed class PasswordValidationException(message: String) : AppException(message) {
    data object TooShort : PasswordValidationException("...")
    data object NoUppercase : PasswordValidationException("...")
    data object NoDigit : PasswordValidationException("...")
}
```
---

## Exception Handling Philosophy

Never throw exceptions for expected failures — always return `Result.Error`. Catch exceptions at the layer that is responsible for the exception:

| Exception origin | Catch in | Example                                                              |
|---|---|----------------------------------------------------------------------|
| HTTP / network | Data layer | `UnknownHostException` → `NoInternetException`                       |
| Database / disk | Data layer | `SQLiteFullException` → `NoFreeSpaceException`                       |
| Business logic | Domain layer | Invalid input → `Result.Error(PasswordValidationException.TooShort)` |
| Presentation | Presentation layer | Catch and map to `Result.Error` at that layer                        |

The layer that owns the exception catches it and converts it to a typed `Result.Error`. Upper layers never see raw exceptions for expected failures.

---

## Mapping Errors to Ui

Every error type that is displayed to the user should have a `.infoScreenConfig` and `.snackbarConfig` extension functions. Place it in:

- **Feature module** — if the error is feature-specific (e.g., `AuthError.infoScreenConfig`)
- **`core:presentation`** — if the error is shared across features (e.g., `DataError.snackbarConfig`)

If an error is purely internal and never shown to the user (e.g., a retry signal, an internal state marker), it does not need a `.infoScreenConfig` and `.snackbarConfig` mapping.

```kotlin
// core:presentation
val DataError.infoScreenConfig: InfoScreenConfig 
    get() {
        return when (this) {
            NetworkException.NoInternetException -> InfoScreenConfig {
                // error screen config
            }
            NetworkException.Unauthorized,
            NetworkException.ServerException -> InfoScreenConfig {
                // error screen config
            }
            LocalException.NoFreeSpaceException -> InfoScreenConfig {
                // error screen config
            }
            // ... map all user-facing cases
            else -> InfoScreenConfig {
                // unkown error screen config
            }
        }
    }
```

---

## Error Mapping (`core:data`)

Map code errors to specific NetworkException of AppException

```kotlin
internal fun getNetworkException(
    response: Response
): NetworkException? {
    return when (response.code()) {
        in 200..299 -> null
        401 -> NetworkException.Unauthorized()
        408 -> NetworkException.RequestTimeout()
        409 -> NetworkException.Conflict()
        413 -> NetworkException.PayloadTooLarge()
        429 -> NetworkException.TooManyRequests()
        in 500..599 -> NetworkException.ServerError()
        else -> NetworkException.Unknown()
    }
}
```

Usage in a data source is clean and uniform:
```kotlin
suspend fun getNotes() = withErrrorHandling<List<NoteDto>> {
    noteApi.getNotes()
}
```

---

The `Result` wrapper is not limited to the data layer — use it anywhere a function has typed success and failure outcomes.
