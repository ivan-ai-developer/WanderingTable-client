package ru.gohasoft.wanderingtable.feature.main.createrequest

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.components.chip.SelectableChip
import ru.gohasoft.wanderingtable.core.uikit.components.chip.SelectableChipVariant
import ru.gohasoft.wanderingtable.core.uikit.components.field.DropdownField
import ru.gohasoft.wanderingtable.core.uikit.components.field.MultilineTextField
import ru.gohasoft.wanderingtable.core.uikit.components.section.FieldLabel
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.stepper.QuantityStepper
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.settings.WatchedGameUi

@Serializable
internal data object CreateRequestScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<CreateRequestViewModel>()) { state ->
            CreateRequestContent(state, ::onEvent)
        }
    }
}

@Composable
private fun CreateRequestContent(
    state: CreateRequestState,
    onEvent: (CreateRequestEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(CreateRequestEvent.OnBackClick) },
            title = stringResource(R.string.create_request_title),
            backContentDescription = stringResource(R.string.main_action_back),
        )

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                // safeDrawing is applied above, before the scroll, so the keyboard shrinks the
                // viewport and the focused note field is scrolled into view.
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            FieldLabel(stringResource(R.string.create_request_label_game))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
            ) {
                state.games.forEach { game ->
                    SelectableChip(
                        text = game.name,
                        variant = if (game.id == state.selectedGameId) {
                            SelectableChipVariant.Selected
                        } else {
                            SelectableChipVariant.Option
                        },
                        onClick = { onEvent(CreateRequestEvent.OnGameSelected(game.id)) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
            ) {
                DropdownField(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.create_request_label_date),
                    value = state.dateLabel,
                    onClick = { onEvent(CreateRequestEvent.OnDateFieldClick) },
                )
                DropdownField(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.create_request_label_time),
                    value = state.timeLabel,
                    onClick = { onEvent(CreateRequestEvent.OnTimeFieldClick) },
                )
            }

            FieldLabel(stringResource(R.string.create_request_label_players))
            QuantityStepper(
                value = state.playersNeeded,
                onDecrement = { onEvent(CreateRequestEvent.OnPlayersDecrement) },
                onIncrement = { onEvent(CreateRequestEvent.OnPlayersIncrement) },
            )

            FieldLabel(stringResource(R.string.create_request_label_skill))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
            ) {
                SkillLevelUi.entries.forEach { level ->
                    SelectableChip(
                        modifier = Modifier.weight(1f),
                        text = stringResource(level.labelRes()),
                        variant = if (level == state.skillLevel) {
                            SelectableChipVariant.Selected
                        } else {
                            SelectableChipVariant.Option
                        },
                        onClick = { onEvent(CreateRequestEvent.OnSkillSelected(level)) },
                    )
                }
            }

            DropdownField(
                label = stringResource(R.string.create_request_label_table),
                value = state.table,
                onClick = { onEvent(CreateRequestEvent.OnTableFieldClick) },
            )

            MultilineTextField(
                label = stringResource(R.string.create_request_label_note),
                value = state.note,
                onValueChange = { note -> onEvent(CreateRequestEvent.OnNoteChanged(note)) },
                placeholder = stringResource(R.string.create_request_note_placeholder),
            )

            state.formError?.let { error ->
                Text(
                    text = error.getText(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (state.isSubmitting) {
                LoadingState()
            } else {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.create_request_submit),
                    onClick = { onEvent(CreateRequestEvent.OnSubmitClick) },
                )
            }
        }
    }

    CreateRequestPickers(state = state, onEvent = onEvent)
}

private fun SkillLevelUi.labelRes(): Int = when (this) {
    SkillLevelUi.BEGINNER -> R.string.create_request_skill_beginner
    SkillLevelUi.ANY -> R.string.create_request_skill_any
    SkillLevelUi.EXPERT -> R.string.create_request_skill_expert
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateRequestContentPreview() {
    WanderingTableTheme {
        CreateRequestContent(
            state = CreateRequestState(
                isLoading = false,
                games = listOf(
                    WatchedGameUi(id = "1", name = "Wingspan"),
                    WatchedGameUi(id = "2", name = "Chess"),
                ),
                selectedGameId = "1",
                dateLabel = "Jul 16",
                timeLabel = "6:00 PM",
                table = "Table 2",
            ),
            onEvent = {},
        )
    }
}
