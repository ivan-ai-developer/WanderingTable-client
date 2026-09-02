package ru.gohasoft.wanderingtable.core.uikit.components.state

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ru.gohasoft.wanderingtable.core.uikit.components.button.SecondaryButton
import ru.gohasoft.wanderingtable.core.uikit.theme.BarlowCondensed
import ru.gohasoft.wanderingtable.core.uikit.theme.Manrope
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.core.uikit.theme.extendedColors

/**
 * What a screen shows instead of content: an empty list, or a load that failed. [actionText] adds
 * a retry affordance; leave it out for an empty state, which has nothing to retry.
 */
@Composable
fun MessageState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(WanderingTableSpacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.s),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = MaterialTheme.extendedColors.caption,
                fontFamily = Manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
        if (actionText != null) {
            SecondaryButton(
                modifier = Modifier.padding(top = WanderingTableSpacing.s),
                text = actionText,
                onClick = onActionClick,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MessageStatePreview() {
    WanderingTableTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            MessageState(
                title = "No open requests",
                subtitle = "Be the first to post one from the Create tab.",
            )
            MessageState(
                title = "Could not load games",
                subtitle = "Check your connection and try again.",
                actionText = "Retry",
            )
        }
    }
}
