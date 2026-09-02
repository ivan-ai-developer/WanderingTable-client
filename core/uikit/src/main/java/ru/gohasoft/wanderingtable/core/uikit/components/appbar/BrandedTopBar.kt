package ru.gohasoft.wanderingtable.core.uikit.components.appbar

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.R
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

/**
 * The club's own bar: logo tile, wordmark, and the bell that leads to the notification feed.
 * [hasUnread] paints the gold dot that is the design's only unread indicator.
 */
@Composable
fun BrandedTopBar(
    title: String,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnread: Boolean = false,
    bellContentDescription: String? = null,
) {
    TopBarRow(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Purple900, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.ic_main_logo),
                tint = Color.White,
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp,
        )
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBellClick,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = bellContentDescription,
                tint = MaterialTheme.colorScheme.onBackground,
            )
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                )
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BrandedTopBarPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            BrandedTopBar(title = "Wandering Table", onBellClick = {}, hasUnread = true)
        }
    }
}
