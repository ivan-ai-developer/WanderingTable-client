package ru.gohasoft.wanderingtable.feature.auth.forgotpassword

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.background.GradientBackground
import ru.gohasoft.wanderingtable.core.uikit.components.button.AuthButton
import ru.gohasoft.wanderingtable.core.uikit.components.field.AuthTextField
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.core.uikit.R as UiKitR

@Serializable
internal data object ForgotPasswordScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<ForgotPasswordViewModel>()) { state ->
            ForgotPasswordContent(state, ::onEvent)
        }
    }
}

@Composable
private fun ForgotPasswordContent(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit,
) {
    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                // safeDrawing (system bars + cutout + IME) must be applied BEFORE
                // verticalScroll so the keyboard shrinks the scroll viewport and the
                // focused field is scrolled into view instead of being covered.
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(WanderingTableSpacing.l),
            verticalArrangement = Arrangement.Center,
        ) {

            Icon(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(100.dp),
                painter = painterResource(UiKitR.drawable.ic_main_logo),
                tint = Color.White,
                contentDescription = null
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.White,
            )

            Spacer(Modifier.height(WanderingTableSpacing.s))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.forgot_password_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(WanderingTableSpacing.l))

            if (state.isLinkSent) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.forgot_password_sent_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                )
            } else {
                AuthTextField(
                    label = stringResource(R.string.forgot_password_email_label),
                    value = state.email,
                    error = state.emailError?.getText() ?: state.generalError?.getText(),
                    onValueChange = { onEvent(ForgotPasswordEvent.OnEmailChanged(it)) },
                    placeholder = stringResource(R.string.forgot_password_email_placeholder),
                    keyboardType = KeyboardType.Email,
                )

                Spacer(Modifier.height(WanderingTableSpacing.l))

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.White,
                    )
                } else {
                    AuthButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.forgot_password_submit_button),
                        onClick = { onEvent(ForgotPasswordEvent.OnSubmitClick) },
                    )
                }
            }

            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = WanderingTableSpacing.m)
                    .clickable { onEvent(ForgotPasswordEvent.OnBackToLoginClick) },
                text = stringResource(R.string.forgot_password_back_link),
                color = Color.White,
            )
        }
    }
}

@Preview
@Composable
private fun ForgotPasswordContentPreview() {
    WanderingTableTheme {
        ForgotPasswordContent(state = ForgotPasswordState(), onEvent = {})
    }
}
