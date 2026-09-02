package ru.gohasoft.wanderingtable.feature.main.createnews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.createnews.CreateNewsEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.createnews.CreateNewsEvent.OnContentChanged
import ru.gohasoft.wanderingtable.feature.main.createnews.CreateNewsEvent.OnSubmitClick
import ru.gohasoft.wanderingtable.feature.main.createnews.CreateNewsEvent.OnTitleChanged
import ru.gohasoft.wanderingtable.feature.main.mapper.toPostNewsError

/**
 * Publishes a club news post.
 *
 * There is no mockup for this screen — it is the destination the Create sheet's "Post Club News"
 * option needs — so it is built from kit primitives against exactly what `POST /notes` accepts:
 * a title and a body.
 */
@HiltViewModel
internal class CreateNewsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val newsRepository: NewsRepository,
) : MviViewModel<CreateNewsState, CreateNewsEvent, Unit>() {

    private val _state = MutableStateFlow(
        CreateNewsState(
            title = savedStateHandle[KEY_TITLE] ?: "",
            content = savedStateHandle[KEY_CONTENT] ?: "",
        )
    )
    override val state: StateFlow<CreateNewsState> = _state.asStateFlow()

    override fun onEvent(event: CreateNewsEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            is OnTitleChanged -> updateTitle(event.title)
            is OnContentChanged -> updateContent(event.content)
            OnSubmitClick -> submit()
        }
    }

    private fun updateTitle(title: String) {
        savedStateHandle[KEY_TITLE] = title
        _state.update { it.copy(title = title, formError = null) }
    }

    private fun updateContent(content: String) {
        savedStateHandle[KEY_CONTENT] = content
        _state.update { it.copy(content = content, formError = null) }
    }

    private fun submit() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(formError = StringResource(R.string.create_news_error_no_title)) }
            return
        }
        if (current.isSubmitting) return

        _state.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            // A null id means "create"; the endpoint is a single upsert keyed by it.
            val result = newsRepository
                .postNews(newsId = null, title = current.title.trim(), content = current.content.trim())
                .firstSuccessOrErrorResult()

            _state.update { it.copy(isSubmitting = false) }
            if (result is Result.Error) {
                _state.update { it.copy(formError = result.error.toPostNewsError()) }
                return@launch
            }
            savedStateHandle.remove<String>(KEY_TITLE)
            savedStateHandle.remove<String>(KEY_CONTENT)
            router.execute(
                ShowSnackbar(
                    SnackbarScreenConfig { message(StringResource(R.string.create_news_posted)) }
                ),
                Back(),
            )
        }
    }

    private companion object {
        const val KEY_TITLE = "draft_title"
        const val KEY_CONTENT = "draft_content"
    }
}
