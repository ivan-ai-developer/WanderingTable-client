package ru.gohasoft.wanderingtable.core.uikit.components.card

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * A club news entry in the Home feed. [badgeText] is optional: the server stores no category for
 * a post, so the badge only appears where a caller can supply one.
 */
@Composable
fun NewsCard(
    title: String,
    excerpt: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    badgeVariant: TagBadgeVariant = TagBadgeVariant.Gold,
) {
    val extended = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .background(extended.tint, RoundedCornerShape(WanderingTableRadius.l))
            .dashedBorder(extended.tintBorder, WanderingTableRadius.l)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (badgeText != null) {
                TagBadge(text = badgeText, variant = badgeVariant)
            }
            Text(
                text = date,
                color = extended.caption,
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            )
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Text(
            text = excerpt,
            color = extended.caption,
            fontFamily = Manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NewsCardPreview() {
    WanderingTableTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
        ) {
            NewsCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Spring Team Championship — Registration Open",
                excerpt = "Sign up in teams of 2 for our biggest tournament of the season.",
                date = "Jul 14",
                badgeText = "Tournament",
                onClick = {},
            )
        }
    }
}
