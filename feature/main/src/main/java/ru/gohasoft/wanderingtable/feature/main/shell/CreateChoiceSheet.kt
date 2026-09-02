package ru.gohasoft.wanderingtable.feature.main.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ru.gohasoft.wanderingtable.core.uikit.components.sheet.ChoiceSheetRow
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.feature.main.R

/**
 * The "What do you want to create?" sheet behind the bottom bar's Create tab.
 *
 * Every player can post a request, but the two catalogue-writing options are role-gated and
 * simply absent otherwise — so no option in this sheet can end in a 403.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateChoiceSheet(
    canPostNews: Boolean,
    canCreateGames: Boolean,
    onPostClubNewsClick: () -> Unit,
    onCreateGameClick: () -> Unit,
    onFindOpponentClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = WanderingTableSpacing.l,
                    end = WanderingTableSpacing.l,
                    bottom = WanderingTableSpacing.l,
                ),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.create_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (canPostNews) {
                ChoiceSheetRow(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = stringResource(R.string.create_sheet_news_title),
                    subtitle = stringResource(R.string.create_sheet_news_subtitle),
                    onClick = onPostClubNewsClick,
                )
            }
            if (canCreateGames) {
                ChoiceSheetRow(
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.create_sheet_game_title),
                    subtitle = stringResource(R.string.create_sheet_game_subtitle),
                    onClick = onCreateGameClick,
                )
            }
            ChoiceSheetRow(
                icon = Icons.Default.Person,
                title = stringResource(R.string.create_sheet_opponent_title),
                subtitle = stringResource(R.string.create_sheet_opponent_subtitle),
                onClick = onFindOpponentClick,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(top = WanderingTableSpacing.s),
                text = stringResource(R.string.create_sheet_cancel),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
