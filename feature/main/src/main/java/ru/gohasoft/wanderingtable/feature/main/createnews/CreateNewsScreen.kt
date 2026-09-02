package ru.gohasoft.wanderingtable.feature.main.createnews

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.getText
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviContent
import ru.gohasoft.wanderingtable.core.uikit.components.appbar.BackTopBar
import ru.gohasoft.wanderingtable.core.uikit.components.button.PrimaryButton
import ru.gohasoft.wanderingtable.core.uikit.components.field.LabeledTextField
import ru.gohasoft.wanderingtable.core.uikit.components.field.MultilineTextField
import ru.gohasoft.wanderingtable.core.uikit.components.state.LoadingState
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableSpacing
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.feature.main.R

@Serializable
internal data object CreateNewsScreen : ComposableScreen() {

    @Composable
    override fun Content() {
        MviContent(hiltViewModel<CreateNewsViewModel>()) { state ->
            CreateNewsContent(state, ::onEvent)
        }
    }
}

@Composable
private fun CreateNewsContent(
    state: CreateNewsState,
    onEvent: (CreateNewsEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BackTopBar(
            onBack = { onEvent(CreateNewsEvent.OnBackClick) },
            title = stringResource(R.string.create_news_title),
            backContentDescription = stringResource(R.string.main_action_back),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WanderingTableSpacing.m),
            verticalArrangement = Arrangement.spacedBy(WanderingTableSpacing.m),
        ) {
            LabeledTextField(
                label = stringResource(R.string.create_news_label_title),
                value = state.title,
                onValueChange = { title -> onEvent(CreateNewsEvent.OnTitleChanged(title)) },
            )
            MultilineTextField(
                label = stringResource(R.string.create_news_label_content),
                value = state.content,
                onValueChange = { content -> onEvent(CreateNewsEvent.OnContentChanged(content)) },
                placeholder = stringResource(R.string.create_news_content_placeholder),
                minHeight = 160.dp,
            )

            state.formError?.let { error ->
                Text(
                    text = error.getText(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.weight(1f))

            if (state.isSubmitting) {
                LoadingState()
            } else {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.create_news_submit),
                    onClick = { onEvent(CreateNewsEvent.OnSubmitClick) },
                )
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateNewsContentPreview() {
    WanderingTableTheme {
        CreateNewsContent(
            state = CreateNewsState(title = "Game Night: Cooperative Games Focus"),
            onEvent = {},
        )
    }
}
