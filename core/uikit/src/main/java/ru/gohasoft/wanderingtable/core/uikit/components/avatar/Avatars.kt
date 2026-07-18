package ru.gohasoft.wanderingtable.core.uikit.components.avatar

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple800
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

@Composable
fun RoundedSquareAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .rotate(-4f)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun CircularRingAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .border(2.dp, Purple800, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun StackedAvatarGroup(
    initials: List<String>,
    extraCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        initials.forEachIndexed { index, text ->
            Box(
                modifier = Modifier
                    .offset(x = (-10 * index).dp)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
        if (extraCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-10 * initials.size).dp)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$extraCount",
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
fun LargeProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
            .border(3.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AvatarsPreview() {
    WanderingTableTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.l),
        ) {
            RoundedSquareAvatar(initials = "AB")
            Spacer(Modifier.width(8.dp))
            CircularRingAvatar(initials = "CD")
            Spacer(Modifier.width(8.dp))
            StackedAvatarGroup(initials = listOf("EF", "GH"), extraCount = 3)
            Spacer(Modifier.width(8.dp))
            LargeProfileAvatar(initials = "IJ")
        }
    }
}
