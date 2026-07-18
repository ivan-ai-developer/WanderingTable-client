package ru.gohasoft.wanderingtable.core.uikit.components.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple800
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple600
import ru.gohasoft.wanderingtable.core.uikit.theme.Purple900
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    watermark: Painter? = null,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to Purple900,
                    0.45f to Purple600,
                    1f to Purple800,
                ),
                RoundedCornerShape(WanderingTableRadius.l),
            ),
    ) {
        if (watermark != null) {
            Image(
                painter = watermark,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .rotate(18f)
                    .alpha(0.14f),
            )
        }
        content()
    }
}

@Preview(name = "GradientBackground")
@Composable
private fun GradientBackgroundPreview() {
    WanderingTableTheme {
        GradientBackground(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Sign In",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
        }
    }
}
