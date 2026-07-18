package ru.gohasoft.wanderingtable.core.uikit.components.navigation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val isCreate: Boolean = false,
)

val WanderingTableNavItems = listOf(
    BottomNavItem(label = "Home", icon = Icons.Default.Home),
    BottomNavItem(label = "Games", icon = Icons.AutoMirrored.Filled.List),
    BottomNavItem(label = "Create", icon = Icons.Default.Add, isCreate = true),
    BottomNavItem(label = "Profile", icon = Icons.Default.Person),
)

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Purple900.copy(alpha = 0.35f),
                spotColor = Purple900.copy(alpha = 0.35f),
            )
            .background(Purple900, RoundedCornerShape(22.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            if (item.isCreate) {
                CreateTab(onClick = { onItemSelected(index) })
            } else {
                NavTab(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = { onItemSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun NavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconColor = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
    val labelColor = if (selected) Color.White else Color.White.copy(alpha = 0.65f)
    val background = if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent

    Column(
        modifier = Modifier
            .background(background, RoundedCornerShape(WanderingTableRadius.m))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(imageVector = item.icon, contentDescription = item.label, tint = iconColor)
        Text(
            text = item.label,
            color = labelColor,
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun CreateTab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.secondary, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Create", tint = Purple900)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BottomNavBarPreview() {
    WanderingTableTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            BottomNavBar(items = WanderingTableNavItems, selectedIndex = 0, onItemSelected = {})
        }
    }
}
