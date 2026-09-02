package ru.gohasoft.wanderingtable.feature.main.fake

import ru.gohasoft.wanderingtable.core.domain.push.PushTokenProvider

/** `null` stands for a build with no messaging configuration — a state the app must tolerate. */
internal class FakePushTokenProvider(private val token: String? = "fcm-token") : PushTokenProvider {
    override suspend fun currentToken(): String? = token
}
