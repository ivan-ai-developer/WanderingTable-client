package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.repository.CacheRepository

internal class FakeCacheRepository : CacheRepository {

    var clearCallCount = 0

    override fun clear(): Flow<Result<Unit>> {
        clearCallCount++
        return flowOf(Result.Success(Unit))
    }
}
