package ru.gohasoft.wanderingtable.core.uikit.components.stat

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

/**
 * One figure of the profile header trio. It always sits on the purple gradient, so the colours
 * are fixed rather than themed.
 */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            color = Color.White,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatTilePreview() {
    WanderingTableTheme {
        Row(
            modifier = Modifier
                .background(Purple900)
                .padding(WanderingTableSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.l),
        ) {
            StatTile(value = "42", label = "Games")
            StatTile(value = "19", label = "Wins")
            StatTile(value = "Strategist", label = "Level")
        }
    }
}
