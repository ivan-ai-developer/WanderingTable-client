package ru.gohasoft.wanderingtable.core.uikit.components.card

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.components.avatar.StackedAvatarGroup
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple800
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

/**
 * The "your next game" card at the top of Home. [content] is the slot the participants row goes
 * into, so the card stays unaware of how players are rendered.
 */
@Composable
fun HeroCard(
    eyebrow: String,
    title: String,
    meta: String,
    modifier: Modifier = Modifier,
    watermark: Painter? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val extended = MaterialTheme.extendedColors
    var cardModifier = modifier
        .background(
            Brush.linearGradient(colors = listOf(Purple800, Purple900)),
            RoundedCornerShape(WanderingTableRadius.l + 2.dp),
        )
    if (onClick != null) {
        cardModifier = cardModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    }

    Column(
        modifier = cardModifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (watermark != null) {
            Image(
                painter = watermark,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.End)
                    .rotate(18f)
                    .alpha(0.16f),
            )
        }
        Text(
            text = eyebrow.uppercase(),
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
        Text(
            text = title,
            color = Color.White,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
        )
        Text(
            text = meta,
            color = extended.textOnGradient,
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
        content()
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeroCardPreview() {
    WanderingTableTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
        ) {
            HeroCard(
                modifier = Modifier.fillMaxWidth(),
                eyebrow = "Your next game",
                title = "Terraforming Mars",
                meta = "Jul 14 · 2:00 PM",
            ) {
                StackedAvatarGroup(initials = listOf("MR", "DK"), extraCount = 0)
            }
        }
    }
}
