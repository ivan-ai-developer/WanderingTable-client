package ru.gohasoft.wanderingtable.core.uikit.components.placeholder

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.components.common.dashedBorder
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableRadius
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

/**
 * The hatched box that stands in for a cover image. News posts carry no image server-side, so
 * this is what News Detail shows in place of one.
 */
@Composable
fun CoverPlaceholder(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
) {
    val extended = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(WanderingTableRadius.l)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(extended.tint, shape)
            .background(
                // A 45-degree repeating stripe, drawn as a tiled linear gradient.
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to extended.tintBorder,
                        0.5f to extended.tint,
                        1.0f to extended.tintBorder,
                    ),
                    start = Offset.Zero,
                    end = Offset(24f, 24f),
                    tileMode = androidx.compose.ui.graphics.TileMode.Repeated,
                ),
                shape = shape,
            )
            .dashedBorder(extended.tintBorder, WanderingTableRadius.l, strokeWidth = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CoverPlaceholderPreview() {
    WanderingTableTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WanderingTableSpacing.m),
        ) {
            CoverPlaceholder(label = "cover photo")
        }
    }
}
