package ru.gohasoft.wanderingtable.feature.main.clubadmin

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.avatar.RoundedSquareAvatar
import ru.gohasoft.wanderingtable.core.uikit.components.button.SecondaryButton
import ru.gohasoft.wanderingtable.core.uikit.components.field.LabeledTextField
import ru.gohasoft.wanderingtable.core.uikit.components.section.SectionCaption
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.state.MessageState
import ru.gohasoft.wanderingtable.core.uikit.components.toggle.ToggleSwitch
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors
import ru.gohasoft.wanderingtable.feature.main.R

/**
 * Club administration, reachable only from the Profile row that the club manager sees.
 *
 * Two sections, both writing through the same endpoint: the manager's own roles — which is how
 * they give themselves `GAME_CREATOR` to fill the catalogue — and role grants to another member
 * looked up by email.
 */
@Serializable
internal data object ClubAdminScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<ClubAdminViewModel>()) { state ->
            ClubAdminContent(state, ::onEvent)
        }
    }
}

@Composable
private fun ClubAdminContent(
    state: ClubAdminState,
    onEvent: (ClubAdminEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(ClubAdminEvent.OnBackClick) },
            title = stringResource(R.string.club_admin_title),
            backContentDescription = stringResource(R.string.main_action_back),
        )

        when {
            state.isLoading -> LoadingState()

            state.error != null -> MessageState(
                title = stringResource(R.string.club_admin_error_title),
                subtitle = state.error.getText(),
                actionText = stringResource(R.string.main_action_retry),
                onActionClick = { onEvent(ClubAdminEvent.OnRetryClick) },
            )

            else -> ClubAdminBody(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun ClubAdminBody(
    state: ClubAdminState,
    onEvent: (ClubAdminEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WanderingTableSpacing.m),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
    ) {
        SectionCaption(stringResource(R.string.club_admin_section_my_roles))
        Text(
            text = stringResource(R.string.club_admin_my_roles_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.extendedColors.caption,
        )
        ClubAdminState.GRANTABLE_ROLES.forEach { role ->
            RoleSwitchRow(
                title = stringResource(role.labelRes()),
                subtitle = stringResource(role.descriptionRes()),
                checked = role in state.myRoles,
                // The server refuses to let a manager drop their own CLUB_MANAGER, so the switch
                // that would do it is inert rather than a guaranteed 409.
                enabled = role != Role.CLUB_MANAGER && state.savingRole == null,
                onCheckedChange = { granted -> onEvent(ClubAdminEvent.OnMyRoleChanged(role, granted)) },
            )
        }

        SectionCaption(stringResource(R.string.club_admin_section_grant))
        LabeledTextField(
            label = stringResource(R.string.club_admin_label_email),
            value = state.email,
            onValueChange = { email -> onEvent(ClubAdminEvent.OnEmailChanged(email)) },
            keyboardType = KeyboardType.Email,
        )
        state.searchError?.let { error ->
            Text(
                text = error.getText(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (state.isSearching) {
            LoadingState()
        } else {
            SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.club_admin_search),
                onClick = { onEvent(ClubAdminEvent.OnSearchClick) },
            )
        }

        state.member?.let { member ->
            MemberCard(member = member, onClear = { onEvent(ClubAdminEvent.OnClearMemberClick) })
            ClubAdminState.GRANTABLE_ROLES.forEach { role ->
                RoleSwitchRow(
                    title = stringResource(role.labelRes()),
                    subtitle = stringResource(role.descriptionRes()),
                    checked = role in member.roles,
                    enabled = state.savingRole == null,
                    onCheckedChange = { granted ->
                        onEvent(ClubAdminEvent.OnMemberRoleChanged(role, granted))
                    },
                )
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: ClubMemberUi,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(WanderingTableRadius.m),
            )
            .padding(WanderingTableSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundedSquareAvatar(initials = member.initials)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = member.email,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.extendedColors.caption,
            )
        }
        SecondaryButton(text = stringResource(R.string.club_admin_clear), onClick = onClear)
    }
}

@Composable
private fun RoleSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(WanderingTableRadius.m),
            )
            .padding(WanderingTableSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.extendedColors.caption,
            )
        }
        ToggleSwitch(
            checked = checked,
            onCheckedChange = { value -> if (enabled) onCheckedChange(value) },
        )
    }
}

private fun Role.labelRes(): Int = when (this) {
    Role.PLAYER -> R.string.club_admin_role_player
    Role.NEWS_CREATOR -> R.string.club_admin_role_news_creator
    Role.GAME_CREATOR -> R.string.club_admin_role_game_creator
    Role.TOURNAMENT_CREATOR -> R.string.club_admin_role_tournament_creator
    Role.CLUB_MANAGER -> R.string.club_admin_role_club_manager
}

private fun Role.descriptionRes(): Int = when (this) {
    Role.PLAYER -> R.string.club_admin_role_player_hint
    Role.NEWS_CREATOR -> R.string.club_admin_role_news_creator_hint
    Role.GAME_CREATOR -> R.string.club_admin_role_game_creator_hint
    Role.TOURNAMENT_CREATOR -> R.string.club_admin_role_tournament_creator_hint
    Role.CLUB_MANAGER -> R.string.club_admin_role_club_manager_hint
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClubAdminContentPreview() {
    WanderingTableTheme {
        ClubAdminContent(
            state = ClubAdminState(
                isLoading = false,
                myUserId = "me",
                myRoles = setOf(Role.PLAYER, Role.CLUB_MANAGER),
                email = "player@example.com",
                member = ClubMemberUi(
                    id = "u2",
                    name = "Mia Rodriguez",
                    email = "player@example.com",
                    initials = "MR",
                    roles = setOf(Role.PLAYER, Role.NEWS_CREATOR),
                ),
            ),
            onEvent = {},
        )
    }
}
