package ru.gohasoft.wanderingtable.core.uikit.components.toggle

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val extended = MaterialTheme.extendedColors
    val trackColor = if (checked) MaterialTheme.colorScheme.secondary else extended.tintBorder
    val knobOffset by animateDpAsState(targetValue = if (checked) 23.dp else 3.dp, label = "toggleKnobOffset")

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 26.dp)
                .background(trackColor, RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCheckedChange(!checked) },
                ),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .offset(x = knobOffset)
                    .size(20.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(Color.White, CircleShape),
            )
        }
        if (label != null) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ToggleSwitchPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(WanderingTableSpacing.m)) {
            ToggleSwitch(checked = true, onCheckedChange = {}, label = "Push notifications")
        }
    }
}
