package ru.gohasoft.wanderingtable.feature.main.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.background.GradientBackground
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R

@Serializable
internal data object WelcomeScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<WelcomeViewModel>()) { state ->
            WelcomeContent(state, ::onEvent)
        }
    }
}

@Composable
private fun WelcomeContent(
    state: WelcomeState,
    onEvent: (WelcomeEvent) -> Unit,
) {
    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(WanderingTableSpacing.l),
            verticalArrangement = Arrangement.spacedBy(
                space = WanderingTableSpacing.l,
                alignment = Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            if (state.userEmail.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.welcome_signed_in_as, state.userEmail),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            PrimaryButton(
                text = stringResource(R.string.welcome_logout_button),
                onClick = { onEvent(WelcomeEvent.OnLogoutClick) },
            )
        }
    }
}

@Preview
@Composable
private fun WelcomeContentPreview() {
    WanderingTableTheme {
        WelcomeContent(state = WelcomeState(userEmail = "player@example.com"), onEvent = {})
    }
}
