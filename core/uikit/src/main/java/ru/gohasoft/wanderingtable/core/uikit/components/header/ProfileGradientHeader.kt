package ru.gohasoft.wanderingtable.core.uikit.components.header

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.components.avatar.LargeProfileAvatar
import ru.gohasoft.wanderingtable.core.uikit.components.stat.StatTile
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple800
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

/** A stat as the profile header shows it: a figure over a gold caption. */
data class ProfileStat(val value: String, val label: String)

/** The gradient block the Profile tab opens with — avatar, name, and three figures. */
@Composable
fun ProfileGradientHeader(
    initials: String,
    name: String,
    subtitle: String,
    stats: List<ProfileStat>,
    modifier: Modifier = Modifier,
) {
    val statusBarHeightDp =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(colors = listOf(Purple900, Purple800)),
                RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            )
            .padding(top = WanderingTableSpacing.l + statusBarHeightDp)
            .padding(bottom = WanderingTableSpacing.l)
            .padding(horizontal = WanderingTableSpacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
    ) {
        LargeProfileAvatar(initials = initials)
        Text(
            text = name,
            color = Color.White,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WanderingTableSpacing.s),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            stats.forEach { stat ->
                StatTile(value = stat.value, label = stat.label)
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileGradientHeaderPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ProfileGradientHeader(
                initials = "AN",
                name = "Alex Novak",
                subtitle = "5 favourite games",
                stats = listOf(
                    ProfileStat(value = "42", label = "Games"),
                    ProfileStat(value = "19", label = "Wins"),
                    ProfileStat(value = "Strategist", label = "Level"),
                ),
            )
        }
    }
}
