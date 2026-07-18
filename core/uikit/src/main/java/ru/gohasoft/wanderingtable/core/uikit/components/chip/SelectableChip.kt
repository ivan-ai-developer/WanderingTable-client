package ru.gohasoft.wanderingtable.core.uikit.components.chip

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.components.common.dashedBorder
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

enum class SelectableChipVariant { Selected, Option, Custom }

@Composable
fun SelectableChip(
    text: String,
    variant: SelectableChipVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(WanderingTableRadius.xl)
    val extended = MaterialTheme.extendedColors

    val backgroundColor = when (variant) {
        SelectableChipVariant.Selected -> MaterialTheme.colorScheme.secondary
        SelectableChipVariant.Option, SelectableChipVariant.Custom -> extended.tint
    }
    val textColor = when (variant) {
        SelectableChipVariant.Selected -> MaterialTheme.colorScheme.onSecondary
        SelectableChipVariant.Option -> MaterialTheme.colorScheme.onSurfaceVariant
        SelectableChipVariant.Custom -> extended.caption
    }

    var chipModifier = modifier
        .background(backgroundColor, shape)
    chipModifier = when (variant) {
        SelectableChipVariant.Option -> chipModifier.border(1.5.dp, extended.tintBorder, shape)
        SelectableChipVariant.Custom -> chipModifier.dashedBorder(extended.tintBorder, WanderingTableRadius.xl)
        SelectableChipVariant.Selected -> chipModifier
    }
    chipModifier = chipModifier
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
        .padding(horizontal = WanderingTableSpacing.m, vertical = WanderingTableSpacing.s)

    Box(modifier = chipModifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = textColor,
            fontFamily = Manrope,
            fontWeight = if (variant == SelectableChipVariant.Selected) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = if (variant == SelectableChipVariant.Selected) 13.sp else 12.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SelectableChipPreview() {
    WanderingTableTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
        ) {
            SelectableChip(text = "Beginner", variant = SelectableChipVariant.Selected, onClick = {})
            SelectableChip(text = "Intermediate", variant = SelectableChipVariant.Option, onClick = {})
            SelectableChip(text = "+ Other", variant = SelectableChipVariant.Custom, onClick = {})
        }
    }
}
