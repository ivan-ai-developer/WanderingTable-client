package ru.gohasoft.wanderingtable.feature.main.creategame

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.components.chip.SelectableChip
import ru.gohasoft.wanderingtable.core.uikit.components.chip.SelectableChipVariant
import ru.gohasoft.wanderingtable.core.uikit.components.field.LabeledTextField
import ru.gohasoft.wanderingtable.core.uikit.components.field.MultilineTextField
import ru.gohasoft.wanderingtable.core.uikit.components.section.FieldLabel
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.components.stepper.QuantityStepper
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors
import ru.gohasoft.wanderingtable.feature.main.R

/**
 * Adds a board game to the club catalogue. Not in the mockups — it is the destination the Create
 * sheet's "Add a Game" option needs, and without it a fresh club has nothing to post requests for.
 */
@Serializable
internal data object CreateGameScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<CreateGameViewModel>()) { state ->
            CreateGameContent(state, ::onEvent)
        }
    }
}

@Composable
private fun CreateGameContent(
    state: CreateGameState,
    onEvent: (CreateGameEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(CreateGameEvent.OnBackClick) },
            title = stringResource(R.string.create_game_title),
            backContentDescription = stringResource(R.string.main_action_back),
        )

        Column(
            modifier = Modifier
                // safeDrawing is applied above, before the scroll, so the keyboard shrinks the
                // viewport and the focused field is scrolled into view.
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            LabeledTextField(
                label = stringResource(R.string.create_game_label_name),
                value = state.name,
                onValueChange = { name -> onEvent(CreateGameEvent.OnNameChanged(name)) },
            )
            MultilineTextField(
                label = stringResource(R.string.create_game_label_description),
                value = state.description,
                onValueChange = { text -> onEvent(CreateGameEvent.OnDescriptionChanged(text)) },
                placeholder = stringResource(R.string.create_game_description_placeholder),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.l),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s)) {
                    FieldLabel(stringResource(R.string.create_game_label_min_players))
                    QuantityStepper(
                        value = state.minPlayers,
                        onDecrement = { onEvent(CreateGameEvent.OnMinPlayersDecrement) },
                        onIncrement = { onEvent(CreateGameEvent.OnMinPlayersIncrement) },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s)) {
                    FieldLabel(stringResource(R.string.create_game_label_max_players))
                    QuantityStepper(
                        value = state.maxPlayers,
                        onDecrement = { onEvent(CreateGameEvent.OnMaxPlayersDecrement) },
                        onIncrement = { onEvent(CreateGameEvent.OnMaxPlayersIncrement) },
                    )
                }
            }

            FieldLabel(stringResource(R.string.create_game_label_result_type))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
            ) {
                GameResultType.entries.forEach { resultType ->
                    SelectableChip(
                        modifier = Modifier.weight(1f),
                        text = stringResource(resultType.labelRes()),
                        variant = if (resultType == state.resultType) {
                            SelectableChipVariant.Selected
                        } else {
                            SelectableChipVariant.Option
                        },
                        onClick = { onEvent(CreateGameEvent.OnResultTypeSelected(resultType)) },
                    )
                }
            }
            // The choice is permanent in practice: it decides what a finished play may report.
            Text(
                text = stringResource(state.resultType.hintRes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.extendedColors.caption,
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
                    text = stringResource(R.string.create_game_submit),
                    onClick = { onEvent(CreateGameEvent.OnSubmitClick) },
                )
            }
        }
    }
}

private fun GameResultType.labelRes(): Int = when (this) {
    GameResultType.WIN_LOSS -> R.string.create_game_result_win_loss
    GameResultType.POINTS -> R.string.create_game_result_points
    GameResultType.PLACEMENT -> R.string.create_game_result_placement
}

private fun GameResultType.hintRes(): Int = when (this) {
    GameResultType.WIN_LOSS -> R.string.create_game_result_win_loss_hint
    GameResultType.POINTS -> R.string.create_game_result_points_hint
    GameResultType.PLACEMENT -> R.string.create_game_result_placement_hint
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateGameContentPreview() {
    WanderingTableTheme {
        CreateGameContent(
            state = CreateGameState(name = "Wingspan", minPlayers = 1, maxPlayers = 5),
            onEvent = {},
        )
    }
}
