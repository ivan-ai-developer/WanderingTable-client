package ru.gohasoft.wanderingtable.core.uikit.components.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

/** A labelled fact on a detail screen: "Date & time", "Location", "Players". */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.xs),
    ) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.extendedColors.label,
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            letterSpacing = 0.55.sp,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailRowPreview() {
    WanderingTableTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            DetailRow(label = "Date & time", value = "Sat, Jul 12 · 7:00 PM")
            DetailRow(label = "Players", value = "1 of 2 joined")
        }
    }
}
