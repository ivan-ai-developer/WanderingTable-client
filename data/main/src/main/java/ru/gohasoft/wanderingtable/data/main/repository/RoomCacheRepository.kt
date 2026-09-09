package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.repository.CacheRepository
import ru.gohasoft.wanderingtable.data.main.local.CacheDataSource

internal class RoomCacheRepository @Inject constructor(
    private val cacheDataSource: CacheDataSource,
) : CacheRepository {

    override fun clear(): Flow<Result<Unit>> = ResultFlow.offlineOnly { cacheDataSource.clear() }
}
