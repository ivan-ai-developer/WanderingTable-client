package ru.gohasoft.wanderingtable.feature.main.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.repository.DeviceRepository

internal class FakeDeviceRepository : DeviceRepository {

    val registeredTokens = mutableListOf<String>()
    val unregisteredTokens = mutableListOf<String>()

    override fun registerPushToken(token: String): Flow<Result<Unit>> {
        registeredTokens += token
        return flowOf(Result.Success(Unit))
    }

    override fun unregisterPushToken(token: String): Flow<Result<Unit>> {
        unregisteredTokens += token
        return flowOf(Result.Success(Unit))
    }
}
