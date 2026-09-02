package ru.gohasoft.wanderingtable.feature.main.createrequest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.gohasoft.wanderingtable.core.uikit.components.list.MenuRow
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.feature.main.R

/**
 * The three pickers Create Request opens. They are platform dialogs rather than kit components:
 * date and time entry is exactly the place where the system's own affordances beat a custom one.
 */
@Composable
internal fun CreateRequestPickers(
    state: CreateRequestState,
    onEvent: (CreateRequestEvent) -> Unit,
) {
    if (state.isDatePickerVisible) {
        StartDatePicker(
            selectedEpochMillis = state.startsAtEpochMillis,
            onPicked = { epochMillis -> onEvent(CreateRequestEvent.OnDatePicked(epochMillis)) },
            onDismiss = { onEvent(CreateRequestEvent.OnDatePickerDismissed) },
        )
    }
    if (state.isTimePickerVisible) {
        StartTimePicker(
            onPicked = { hour, minute -> onEvent(CreateRequestEvent.OnTimePicked(hour, minute)) },
            onDismiss = { onEvent(CreateRequestEvent.OnTimePickerDismissed) },
        )
    }
    if (state.isTablePickerVisible) {
        TablePicker(
            onPicked = { table -> onEvent(CreateRequestEvent.OnTablePicked(table)) },
            onDismiss = { onEvent(CreateRequestEvent.OnTablePickerDismissed) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDatePicker(
    selectedEpochMillis: Long,
    onPicked: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedEpochMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let(onPicked) ?: onDismiss() },
            ) {
                Text(stringResource(R.string.main_action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.main_action_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartTimePicker(
    onPicked: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onPicked(pickerState.hour, pickerState.minute) }) {
                Text(stringResource(R.string.main_action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.main_action_cancel))
            }
        },
        text = { TimePicker(state = pickerState) },
    )
}

/**
 * Tables are a fixed club-side list: the server stores no venue for a play, so this choice never
 * leaves the device. See [SkillLevelUi] for the same trade-off on skill level.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TablePicker(
    onPicked: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val tables = List(CLUB_TABLE_COUNT) { index -> "Table ${index + 1}" }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        LazyColumn(
            modifier = Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = WanderingTableSpacing.l,
                end = WanderingTableSpacing.l,
                bottom = WanderingTableSpacing.l,
            ),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(R.string.create_request_table_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(items = tables, key = { it }) { table ->
                MenuRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = table,
                    onClick = { onPicked(table) },
                )
            }
        }
    }
}

private const val CLUB_TABLE_COUNT = 6
