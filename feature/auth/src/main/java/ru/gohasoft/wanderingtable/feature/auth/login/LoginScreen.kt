package ru.gohasoft.wanderingtable.feature.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
internal data object LoginScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<LoginViewModel>()) { state ->
            LoginContent(state, ::onEvent)
        }
    }
}

@Composable
private fun LoginContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
) {
    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(WanderingTableSpacing.l),
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
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.White,
            )

            Spacer(Modifier.height(WanderingTableSpacing.s))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(WanderingTableSpacing.l))

            AuthTextField(
                label = stringResource(R.string.login_email_label),
                value = state.email,
                error = state.emailError?.getText(),
                onValueChange = { onEvent(LoginEvent.OnEmailChanged(it)) },
                placeholder = stringResource(R.string.login_email_placeholder),
                keyboardType = KeyboardType.Email,
            )

            Spacer(Modifier.height(WanderingTableSpacing.m))

            AuthTextField(
                label = stringResource(R.string.login_password_label),
                value = state.password,
                error = state.passwordError?.getText() ?: state.generalError?.getText(),
                onValueChange = { onEvent(LoginEvent.OnPasswordChanged(it)) },
                placeholder = stringResource(R.string.login_password_placeholder),
                isPassword = true,
            )

            Spacer(Modifier.height(WanderingTableSpacing.s))

            Text(
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onEvent(LoginEvent.OnForgotPasswordClick) },
                text = stringResource(R.string.login_forgot_password_link),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
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
                    text = stringResource(R.string.login_submit_button),
                    onClick = { onEvent(LoginEvent.OnLoginClick) },
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = WanderingTableSpacing.m),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.login_signup_prompt),
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = stringResource(R.string.login_signup_link),
                    color = Color.White,
                    modifier = Modifier.clickable { onEvent(LoginEvent.OnSignUpClick) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginContentPreview() {
    WanderingTableTheme {
        LoginContent(state = LoginState(), onEvent = {})
    }
}
