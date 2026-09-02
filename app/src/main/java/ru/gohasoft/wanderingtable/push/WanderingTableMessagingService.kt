package ru.gohasoft.wanderingtable.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.R
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationSettings
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationType
import ru.gohasoft.wanderingtable.core.domain.repository.DeviceRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationSettingsRepository

/**
 * Receives pushes and turns them into two things: an entry in the in-app feed, and — when the
 * user's preferences allow it — a system notification.
 *
 * The feed is written regardless of those preferences: switching a topic off means "stop
 * interrupting me", not "hide it from the app".
 */
@AndroidEntryPoint
internal class WanderingTableMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var settingsRepository: NotificationSettingsRepository

    /** Service callbacks are not suspending, and the work outlives none of them. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /**
     * Fired on install, on token rotation, and after a data wipe. Re-registering is idempotent
     * server-side, so it is safe to send every time.
     */
    override fun onNewToken(token: String) {
        scope.launch { deviceRepository.registerPushToken(token).firstSuccessOrErrorData() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.toNotification()
        scope.launch {
            notificationRepository.add(notification).firstSuccessOrErrorData()
            val settings = settingsRepository.getSettings().firstSuccessOrErrorData()
                ?: NotificationSettings()
            if (settings.allows(notification.type)) {
                showSystemNotification(notification)
            }
        }
    }

    /**
     * Reads the data payload first: a notification payload is only delivered to the app while it
     * is in the foreground, so the server is expected to send data messages.
     */
    private fun RemoteMessage.toNotification(): Notification {
        val type = data[KEY_TYPE]
            ?.let { name -> NotificationType.entries.firstOrNull { it.name == name } }
            ?: NotificationType.GENERAL
        return Notification(
            id = messageId ?: UUID.randomUUID().toString(),
            title = data[KEY_TITLE] ?: notification?.title.orEmpty(),
            message = data[KEY_BODY] ?: notification?.body.orEmpty(),
            createdAt = Instant.now(),
            isRead = false,
            type = type,
        )
    }

    private fun NotificationSettings.allows(type: NotificationType): Boolean = when {
        !pushEnabled -> false
        type == NotificationType.NEWS -> clubNews
        type == NotificationType.SESSION_REMINDER -> gameReminders
        type == NotificationType.OPPONENT_FOUND || type == NotificationType.SESSION_INVITE ->
            gameInvites

        else -> true
    }

    private fun showSystemNotification(notification: Notification) {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        // Below API 33 the permission does not exist and the check above always passes.
        if (!granted) return

        ensureChannel()
        val system = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(notification.id.hashCode(), system)
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.push_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = getString(R.string.push_channel_description) }
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "wandering_table_club"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_TYPE = "type"
    }
}
