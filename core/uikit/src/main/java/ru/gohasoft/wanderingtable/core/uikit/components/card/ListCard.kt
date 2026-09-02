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

/**
 * The card the Games list is built from. Callers size it — the Games screen stretches it to the
 * full width, a horizontal rail would constrain it instead.
 *
 * [ctaText] is optional because a request you host has no action on it; that card uses
 * [ListCardVariant.Highlighted] to read as yours.
 */
@Composable
fun ListCard(
    title: String,
    meta: String,
    modifier: Modifier = Modifier,
    hostLine: String? = null,
    ctaText: String? = null,
    onCtaClick: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    variant: ListCardVariant = ListCardVariant.Borderless,
    badgeText: String? = null,
    badgeVariant: TagBadgeVariant = TagBadgeVariant.Gold,
) {
    val extended = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(WanderingTableRadius.l)
    val backgroundColor = if (variant == ListCardVariant.Highlighted) {
        extended.goldTint
    } else {
        MaterialTheme.colorScheme.surface
    }

    var cardModifier = modifier.background(backgroundColor, shape)
    cardModifier = when (variant) {
        ListCardVariant.Outlined -> cardModifier.dashedBorder(extended.tintBorder, WanderingTableRadius.l)
        ListCardVariant.Highlighted -> cardModifier.dashedBorder(extended.goldTintBorder, WanderingTableRadius.l)
        ListCardVariant.Borderless -> cardModifier
    }
    if (onClick != null) {
        cardModifier = cardModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                if (hostLine != null) {
                    Text(
                        text = hostLine,
                        color = extended.caption,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }
            }
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
        if (ctaText != null) {
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
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListCardPreview() {
    WanderingTableTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            ListCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Settlers of Catan",
                hostLine = "Hosted by a club member",
                meta = "Sat, Jul 12 · 7:00 PM · needs 1 of 2",
                ctaText = "Join Game",
                onCtaClick = {},
                variant = ListCardVariant.Outlined,
                badgeText = "Any level",
            )
            ListCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Terraforming Mars",
                hostLine = "Hosted by you",
                meta = "Jul 14 · 2:00 PM · needs 1 of 3",
                variant = ListCardVariant.Highlighted,
                badgeText = "Expert",
                badgeVariant = TagBadgeVariant.Purple800,
            )
        }
    }
}
