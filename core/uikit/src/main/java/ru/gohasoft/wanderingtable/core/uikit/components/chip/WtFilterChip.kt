package ru.gohasoft.wanderingtable.core.uikit.components.chip

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

@Composable
fun WtFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(WanderingTableRadius.xl)
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.extendedColors.tint
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.extendedColors.label

    Box(
        modifier = modifier
            .height(44.dp)
            .background(backgroundColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = WanderingTableSpacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WtFilterChipPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(WanderingTableSpacing.m)) {
            WtFilterChip(text = "Chess", selected = true, onClick = {})
        }
    }
}
