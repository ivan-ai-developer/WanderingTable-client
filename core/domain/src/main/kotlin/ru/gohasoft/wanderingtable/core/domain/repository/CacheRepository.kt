package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result

/**
 * The offline copy of server data, as one thing that can be dropped.
 *
 * It exists for logout: the cached profile, schedule and feed belong to the account that was
 * signed in, and the next account on the same device must not see them. Nothing else clears it —
 * every other write happens as part of a successful fetch.
 */
interface CacheRepository {

    fun clear(): Flow<Result<Unit>>
}
