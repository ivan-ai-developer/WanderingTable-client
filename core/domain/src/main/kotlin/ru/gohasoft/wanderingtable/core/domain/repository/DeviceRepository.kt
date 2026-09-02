package ru.gohasoft.wanderingtable.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.gohasoft.wanderingtable.core.domain.Result

/** Binds this device's push token to the signed-in account. */
interface DeviceRepository {

    /** Idempotent: re-registering the same token moves it to the current account. */
    fun registerPushToken(token: String): Flow<Result<Unit>>

    /** Call on logout so the account stops receiving pushes on this device. */
    fun unregisterPushToken(token: String): Flow<Result<Unit>>
}
