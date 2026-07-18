package ru.gohasoft.wanderingtable.core.uikit.components.badge

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple800
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

enum class TagBadgeVariant { Gold, Purple600, Purple800 }

@Composable
fun TagBadge(
    text: String,
    variant: TagBadgeVariant,
    modifier: Modifier = Modifier,
    rotationDegrees: Float = -2f,
) {
    val backgroundColor = when (variant) {
        TagBadgeVariant.Gold -> MaterialTheme.colorScheme.secondary
        TagBadgeVariant.Purple600 -> MaterialTheme.colorScheme.primary
        TagBadgeVariant.Purple800 -> Purple800
    }
    val textColor = when (variant) {
        TagBadgeVariant.Gold -> Purple900
        TagBadgeVariant.Purple600, TagBadgeVariant.Purple800 -> Color.White
    }

    Box(
        modifier = modifier
            .rotate(rotationDegrees)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            letterSpacing = 0.3.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TagBadgePreview() {
    WanderingTableTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.l),
        ) {
            TagBadge(text = "Any level", variant = TagBadgeVariant.Gold)
            TagBadge(text = "Intermediate", variant = TagBadgeVariant.Purple600)
            TagBadge(text = "Expert", variant = TagBadgeVariant.Purple800)
        }
    }
}
