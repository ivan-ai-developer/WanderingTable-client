package ru.gohasoft.wanderingtable.core.uikit.components.stepper

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

@Composable
private fun StepperGlyphButton(
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .background(MaterialTheme.colorScheme.secondary, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            color = Purple900,
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun QuantityStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .height(50.dp)
            .background(extended.tint, RoundedCornerShape(WanderingTableRadius.m))
            .border(2.dp, extended.tintBorder, RoundedCornerShape(WanderingTableRadius.m))
            .padding(horizontal = WanderingTableSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
    ) {
        StepperGlyphButton(glyph = "−", onClick = onDecrement)
        Text(
            text = value.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
        )
        StepperGlyphButton(glyph = "+", onClick = onIncrement)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuantityStepperPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(WanderingTableSpacing.m)) {
            QuantityStepper(value = 4, onDecrement = {}, onIncrement = {})
        }
    }
}
