package ru.gohasoft.wanderingtable.data.main.remote.api

import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.PUT
import ru.gohasoft.wanderingtable.data.main.remote.dto.DeviceDto
import ru.gohasoft.wanderingtable.data.main.remote.dto.DeviceTokenRequestDto

internal interface DevicesApi {

    /** Idempotent: re-registering a token moves it to the calling account. */
    @PUT("users/me/devices")
    suspend fun register(@Body body: DeviceTokenRequestDto): DeviceDto

    /** The unbind endpoint takes a body, which `@DELETE` cannot carry. */
    @HTTP(method = "DELETE", path = "users/me/devices", hasBody = true)
    suspend fun unregister(@Body body: DeviceTokenRequestDto)
}
