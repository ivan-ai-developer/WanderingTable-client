package ru.gohasoft.wanderingtable.data.main.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.data.repository.ResultFlow
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.repository.DeviceRepository
import ru.gohasoft.wanderingtable.data.main.remote.RemoteDataSource

internal class NetworkDeviceRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : DeviceRepository {

    override fun registerPushToken(token: String): Flow<Result<Unit>> =
        ResultFlow.onlineOnly { remoteDataSource.registerDevice(token) }

    override fun unregisterPushToken(token: String): Flow<Result<Unit>> =
        ResultFlow.onlineOnly { remoteDataSource.unregisterDevice(token) }
}
