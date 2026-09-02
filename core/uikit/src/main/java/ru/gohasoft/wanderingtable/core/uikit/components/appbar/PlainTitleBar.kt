package ru.gohasoft.wanderingtable.core.uikit.components.appbar

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

/** A page heading with no navigation affordance — used by tabs, which sit at the root. */
@Composable
fun PlainTitleBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    TopBarRow(modifier = modifier) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlainTitleBarPreview() {
    WanderingTableTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            PlainTitleBar(title = "Games")
        }
    }
}
