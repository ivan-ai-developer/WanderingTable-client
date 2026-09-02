package ru.gohasoft.wanderingtable.core.uikit.components.list

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
import ru.gohasoft.wanderingtable.core.uikit.components.avatar.RoundedSquareAvatar
import ru.gohasoft.wanderingtable.core.uikit.components.common.dashedBorder
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

/**
 * One entry of the notification feed. [highlighted] paints the gold-tinted variant the design
 * uses for things that need acting on soon, such as a game about to start.
 */
@Composable
fun NotificationRow(
    initials: String,
    title: String,
    subtitle: String,
    timestamp: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val extended = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(WanderingTableRadius.m)
    var rowModifier = modifier.background(
        if (highlighted) extended.goldTint else MaterialTheme.colorScheme.surface,
        shape,
    )
    if (highlighted) {
        rowModifier = rowModifier.dashedBorder(extended.goldTintBorder, WanderingTableRadius.m)
    }
    if (onClick != null) {
        rowModifier = rowModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    }

    Row(
        modifier = rowModifier.padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundedSquareAvatar(initials = initials)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = subtitle,
                color = extended.caption,
                fontFamily = Manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = timestamp,
            color = extended.caption,
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationRowPreview() {
    WanderingTableTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
        ) {
            NotificationRow(
                modifier = Modifier.fillMaxWidth(),
                initials = "MR",
                title = "Opponent found for Catan!",
                subtitle = "Someone joined your request for Sat, 7:00 PM",
                timestamp = "2h ago",
            )
            NotificationRow(
                modifier = Modifier.fillMaxWidth(),
                initials = "!",
                title = "Game starting soon",
                subtitle = "Your Chess match starts in 1 hour",
                timestamp = "3h ago",
                highlighted = true,
            )
        }
    }
}
