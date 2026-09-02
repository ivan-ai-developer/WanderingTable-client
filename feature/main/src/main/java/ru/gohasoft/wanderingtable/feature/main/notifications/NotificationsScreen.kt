package ru.gohasoft.wanderingtable.feature.main.notifications

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.list.NotificationRow
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.model.NotificationGroup
import ru.gohasoft.wanderingtable.feature.main.model.NotificationUi

@Serializable
internal data object NotificationsScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        RequestPostNotificationsPermission()
        MviContent(hiltViewModel<NotificationsViewModel>()) { state ->
            NotificationsContent(state, ::onEvent)
        }
    }
}

/**
 * Asked for here rather than at launch: this is the first place the user shows they care about
 * notifications, which is when the system prompt makes sense. Denying it only silences the system
 * tray — the in-app feed still fills up.
 */
@Composable
private fun RequestPostNotificationsPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun NotificationsContent(
    state: NotificationsState,
    onEvent: (NotificationsEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(NotificationsEvent.OnBackClick) },
            title = stringResource(R.string.notifications_title),
            backContentDescription = stringResource(R.string.main_action_back),
        )

        when {
            state.isLoading -> LoadingState()

            state.isEmpty -> MessageState(
                title = stringResource(R.string.notifications_empty_title),
                subtitle = stringResource(R.string.notifications_empty_subtitle),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(WanderingTableSpacing.m),
                verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
            ) {
                notificationGroup(
                    titleRes = R.string.notifications_group_today,
                    notifications = state.today,
                    onEvent = onEvent,
                )
                notificationGroup(
                    titleRes = R.string.notifications_group_earlier,
                    notifications = state.earlier,
                    onEvent = onEvent,
                )
            }
        }
    }
}

private fun LazyListScope.notificationGroup(
    titleRes: Int,
    notifications: List<NotificationUi>,
    onEvent: (NotificationsEvent) -> Unit,
) {
    if (notifications.isEmpty()) return
    item(key = "group_$titleRes") {
        Text(
            text = stringResource(titleRes).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    items(items = notifications, key = NotificationUi::id) { notification ->
        NotificationRow(
            modifier = Modifier.fillMaxWidth(),
            initials = notification.initials,
            title = notification.title,
            subtitle = notification.message,
            timestamp = notification.timestamp.getText(),
            highlighted = notification.highlighted,
            onClick = { onEvent(NotificationsEvent.OnNotificationClick(notification.id)) },
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationsContentPreview() {
    WanderingTableTheme {
        NotificationsContent(
            state = NotificationsState(
                isLoading = false,
                notifications = listOf(
                    NotificationUi(
                        id = "1",
                        initials = "VS",
                        title = "Opponent found for Catan!",
                        message = "Someone joined your request for Sat, 7:00 PM",
                        timestamp = TextResource.DynamicString("2h ago"),
                        isRead = false,
                        highlighted = false,
                        group = NotificationGroup.TODAY,
                    ),
                    NotificationUi(
                        id = "2",
                        initials = "!",
                        title = "Game starting soon",
                        message = "Your Chess match starts in 1 hour",
                        timestamp = TextResource.DynamicString("Yesterday"),
                        isRead = true,
                        highlighted = true,
                        group = NotificationGroup.EARLIER,
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}
