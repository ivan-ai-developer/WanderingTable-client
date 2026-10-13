package ru.gohasoft.wanderingtable.data.auth.fake

import ru.gohasoft.wanderingtable.core.data.cache.CacheCleaner

internal class FakeCacheCleaner(private val error: Throwable? = null) : CacheCleaner {

    var clearCallCount = 0

    override suspend fun clear() {
        clearCallCount++
        error?.let { throw it }
    }
}
