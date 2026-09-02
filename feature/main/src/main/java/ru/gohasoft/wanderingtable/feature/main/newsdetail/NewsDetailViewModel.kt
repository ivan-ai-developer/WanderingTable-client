package ru.gohasoft.wanderingtable.feature.main.newsdetail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.mapper.toLoadError
import ru.gohasoft.wanderingtable.feature.main.mapper.toNewsItemUi
import ru.gohasoft.wanderingtable.feature.main.newsdetail.NewsDetailEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.newsdetail.NewsDetailEvent.OnRetryClick

@HiltViewModel(assistedFactory = NewsDetailViewModel.Factory::class)
internal class NewsDetailViewModel @AssistedInject constructor(
    @Assisted private val screen: NewsDetailScreen,
    private val router: Router,
    private val authRepository: AuthRepository,
    private val newsRepository: NewsRepository,
) : MviViewModel<NewsDetailState, NewsDetailEvent, Unit>() {

    @AssistedFactory
    interface Factory {
        fun create(screen: NewsDetailScreen): NewsDetailViewModel
    }

    private val _state = MutableStateFlow(NewsDetailState())
    override val state: StateFlow<NewsDetailState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onEvent(event: NewsDetailEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            OnRetryClick -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getSession().firstSuccessOrErrorData()?.user?.id
            val result = newsRepository.getNewsItem(screen.newsId).firstSuccessOrErrorResult()
            _state.update { current ->
                when (result) {
                    is Result.Error -> current.copy(
                        isLoading = false,
                        error = result.error.toLoadError(),
                    )

                    else -> current.copy(
                        isLoading = false,
                        error = null,
                        news = result?.data?.toNewsItemUi(currentUserId = userId),
                    )
                }
            }
        }
    }
}
