package ru.gohasoft.wanderingtable.feature.auth.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import ru.gohasoft.wanderingtable.core.uikit.R as UiKitR
import ru.gohasoft.wanderingtable.core.uikit.components.background.GradientBackground
import ru.gohasoft.wanderingtable.core.uikit.components.button.AuthButton
import ru.gohasoft.wanderingtable.core.uikit.components.field.AuthTextField
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.auth.R

@Serializable
internal data object SignUpScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<SignUpViewModel>()) { state ->
            SignUpContent(state, ::onEvent)
        }
    }
}

@Composable
private fun SignUpContent(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
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
                text = stringResource(R.string.signup_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.White,
            )

            Spacer(Modifier.height(WanderingTableSpacing.s))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.signup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(WanderingTableSpacing.l))

            AuthTextField(
                label = stringResource(R.string.signup_name_label),
                value = state.name,
                onValueChange = { onEvent(SignUpEvent.OnNameChanged(it)) },
                placeholder = stringResource(R.string.signup_name_placeholder),
                error = state.nameError?.getText()
            )

            Spacer(Modifier.height(WanderingTableSpacing.m))

            AuthTextField(
                label = stringResource(R.string.signup_email_label),
                value = state.email,
                onValueChange = { onEvent(SignUpEvent.OnEmailChanged(it)) },
                placeholder = stringResource(R.string.signup_email_placeholder),
                error = state.emailError?.getText(),
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(WanderingTableSpacing.m))

            AuthTextField(
                label = stringResource(R.string.signup_password_label),
                value = state.password,
                onValueChange = { onEvent(SignUpEvent.OnPasswordChanged(it)) },
                placeholder = stringResource(R.string.signup_password_placeholder),
                error = state.passwordError?.getText(),
                isPassword = true
            )

            Spacer(Modifier.height(WanderingTableSpacing.m))

            AuthTextField(
                label = stringResource(R.string.signup_confirm_password_label),
                value = state.confirmPassword,
                onValueChange = { onEvent(SignUpEvent.OnConfirmPasswordChanged(it)) },
                placeholder = stringResource(R.string.signup_confirm_password_placeholder),
                error = state.confirmPasswordError?.getText()
                    ?: state.generalError?.getText(),
                isPassword = true
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
                    text = stringResource(R.string.signup_submit_button),
                    onClick = { onEvent(SignUpEvent.OnSignUpClick) }
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = WanderingTableSpacing.m),
                horizontalArrangement = Arrangement.spacedBy(WanderingTableSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.signup_login_prompt),
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = stringResource(R.string.signup_login_link),
                    color = Color.White,
                    modifier = Modifier.clickable { onEvent(SignUpEvent.OnLoginClick) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun SignUpContentPreview() {
    WanderingTableTheme {
        SignUpContent(state = SignUpState(), onEvent = {})
    }
}
