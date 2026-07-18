package ru.gohasoft.wanderingtable.core.uikit.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(WanderingTableRadius.xl)
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            )
            .background(MaterialTheme.colorScheme.secondary, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = WanderingTableSpacing.l, vertical = WanderingTableSpacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.onSecondary,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            letterSpacing = 0.75.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrimaryButtonPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(Color.Transparent).padding(WanderingTableSpacing.m)) {
            PrimaryButton(text = "Join Game", onClick = {})
        }
    }
}
