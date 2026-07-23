package ru.gohasoft.wanderingtable.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.background.GradientBackground
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.welcome.WelcomeEvent.OnShowSnackbarClick

/**
 * Временный тестовый экран приветствия: проверяет конвейер
 * NavigationHost -> Router -> Command -> Snackbar. Удалить при появлении первых фич.
 */
@Serializable
class WelcomeScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(
            hiltViewModel<WelcomeViewModel>()
        ) { state ->
            WelcomeContent(state, ::onEvent)
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
                    .align(Alignment.Center)
                    .padding(WanderingTableSpacing.l),
                verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.title.getText(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
                Text(
                    text = state.subtitle.getText(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
                PrimaryButton(
                    text = state.buttonText.getText(),
                    onClick = { onEvent(OnShowSnackbarClick) },
                )
            }
        }
    }

    @Preview
    @Composable
    private fun WelcomeContentPreview() {
        WanderingTableTheme {
            WelcomeContent(state = WelcomeState(), onEvent = {})
        }
    }
}


