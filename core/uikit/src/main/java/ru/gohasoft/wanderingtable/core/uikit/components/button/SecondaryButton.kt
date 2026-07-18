package ru.gohasoft.wanderingtable.core.uikit.components.button

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(WanderingTableRadius.xl)
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = WanderingTableSpacing.m, vertical = WanderingTableSpacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            letterSpacing = 0.65.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SecondaryButtonPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(WanderingTableSpacing.m)) {
            SecondaryButton(text = "Join Game", onClick = {})
        }
    }
}
