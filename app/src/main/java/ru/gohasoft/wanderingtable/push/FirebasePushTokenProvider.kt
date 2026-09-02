package ru.gohasoft.wanderingtable.push

import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import ru.gohasoft.wanderingtable.core.domain.push.PushTokenProvider

/**
 * Reads the FCM registration token.
 *
 * Everything here is best effort by design: without a bundled `google-services.json` the SDK has
 * no project to initialise against and throws on first use. That is a supported state for this
 * app — push is simply off — so the failure is swallowed and reported as "no token".
 */
@Singleton
internal class FirebasePushTokenProvider @Inject constructor() : PushTokenProvider {

    override suspend fun currentToken(): String? = runCatching {
        FirebaseMessaging.getInstance().token.await()
    }.getOrNull()
}
