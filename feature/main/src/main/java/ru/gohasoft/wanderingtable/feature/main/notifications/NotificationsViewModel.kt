package ru.gohasoft.wanderingtable.feature.main.notifications

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.mapper.toNotificationUi
import ru.gohasoft.wanderingtable.feature.main.notifications.NotificationsEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.notifications.NotificationsEvent.OnNotificationClick

/**
 * The push feed, read from local storage as a live flow — a push that arrives while the screen is
 * open appears without a refresh.
 *
 * A row has nothing to open: pushes carry a message, not a destination, so tapping one only marks
 * it read. That is also what clears the bell's unread dot on Home.
 */
@HiltViewModel
internal class NotificationsViewModel @Inject constructor(
    private val router: Router,
    private val notificationRepository: NotificationRepository,
) : MviViewModel<NotificationsState, NotificationsEvent, Unit>() {

    private val _state = MutableStateFlow(NotificationsState())
    override val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        observeNotifications()
    }

    override fun onEvent(event: NotificationsEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            is OnNotificationClick -> markAsRead(event.notificationId)
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepository.getNotifications().collect { result ->
                val notifications = result.data ?: return@collect
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        notifications = notifications.map { notification: Notification ->
                            notification.toNotificationUi()
                        },
                    )
                }
            }
        }
    }

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId).firstSuccessOrErrorResult()
        }
    }
}
