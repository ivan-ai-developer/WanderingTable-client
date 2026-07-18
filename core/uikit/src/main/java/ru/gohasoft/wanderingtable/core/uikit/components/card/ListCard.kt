package ru.gohasoft.wanderingtable.core.uikit.components.card

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import ru.gohasoft.wanderingtable.core.uikit.components.badge.TagBadge
import ru.gohasoft.wanderingtable.core.uikit.components.badge.TagBadgeVariant
import ru.gohasoft.wanderingtable.core.uikit.components.common.dashedBorder
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

enum class ListCardVariant { Outlined, Borderless, Highlighted }

@Composable
fun ListCard(
    title: String,
    meta: String,
    ctaText: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ListCardVariant = ListCardVariant.Borderless,
    badgeText: String? = null,
    badgeVariant: TagBadgeVariant = TagBadgeVariant.Gold,
) {
    val extended = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(WanderingTableRadius.l)
    val backgroundColor = if (variant == ListCardVariant.Highlighted) extended.goldTint else MaterialTheme.colorScheme.surface

    var cardModifier = modifier
        .width(280.dp)
        .background(backgroundColor, shape)
    cardModifier = when (variant) {
        ListCardVariant.Outlined -> cardModifier.dashedBorder(extended.tintBorder, WanderingTableRadius.l)
        ListCardVariant.Highlighted -> cardModifier.dashedBorder(extended.goldTintBorder, WanderingTableRadius.l)
        ListCardVariant.Borderless -> cardModifier
    }

    Column(
        modifier = cardModifier.padding(horizontal = 16.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            if (badgeText != null) {
                TagBadge(text = badgeText, variant = badgeVariant, rotationDegrees = -3f)
            }
        }
        Text(
            text = meta,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(WanderingTableRadius.xl))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCtaClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = ctaText.uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 0.65.sp,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListCardPreview() {
    WanderingTableTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
            horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            ListCard(
                title = "Chess Night",
                meta = "Fri · 7:00 PM · Table 3",
                ctaText = "Join Game",
                onCtaClick = {},
                variant = ListCardVariant.Outlined,
                badgeText = "Any level",
            )
            ListCard(
                title = "Wingspan",
                meta = "Sat · 2:00 PM · Table 1",
                ctaText = "Join Game",
                onCtaClick = {},
                variant = ListCardVariant.Highlighted,
                badgeText = "Expert",
                badgeVariant = TagBadgeVariant.Purple800,
            )
        }
    }
}
