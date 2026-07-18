package ru.gohasoft.wanderingtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.components.card.ListCard
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WanderingTableTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UikitShowcase(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UikitShowcase(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WanderingTableSpacing.l),
        verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ListCard(
            title = "Chess Night",
            meta = "Fri · 7:00 PM · Table 3",
            ctaText = "Join Game",
            onCtaClick = {},
            badgeText = "Any level",
        )
        PrimaryButton(text = "Host a Game", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun UikitShowcasePreview() {
    WanderingTableTheme {
        UikitShowcase()
    }
}
