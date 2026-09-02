package ru.gohasoft.wanderingtable.core.domain.push

/**
 * Reads this device's push registration token.
 *
 * The token comes from a platform SDK, so the implementation lives in `:app` — the same seam
 * `:core:network` uses for its token pipeline. Feature code depends on this interface and stays
 * free of any messaging dependency.
 */
interface PushTokenProvider {

    /**
     * Null when push is unavailable on this build or device — no messaging configuration bundled,
     * or the services the SDK needs are missing. Callers treat that as "nothing to unregister"
     * rather than as an error.
     */
    suspend fun currentToken(): String?
}
