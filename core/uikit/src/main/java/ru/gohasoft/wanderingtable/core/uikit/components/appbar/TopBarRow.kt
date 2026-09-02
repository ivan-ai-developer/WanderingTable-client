package ru.gohasoft.wanderingtable.core.uikit.components.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing

/** The shared geometry of every top bar in the app, so the three variants line up with each other. */
@Composable
internal fun TopBarRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WanderingTableSpacing.m, vertical = WanderingTableSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
