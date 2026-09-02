package ru.gohasoft.wanderingtable.feature.main.creategame

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
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnDescriptionChanged
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnMaxPlayersDecrement
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnMaxPlayersIncrement
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnMinPlayersDecrement
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnMinPlayersIncrement
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnNameChanged
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnResultTypeSelected
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameEvent.OnSubmitClick
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameState.Companion.MAX_PLAYERS_LIMIT
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameState.Companion.MIN_PLAYERS_LIMIT
import ru.gohasoft.wanderingtable.feature.main.mapper.toCreateGameError

/**
 * Adds a game to the club catalogue.
 *
 * Nothing can be posted to `/events/regular-games` until the catalogue has an entry, so this is
 * the screen that unblocks "Find an Opponent" on a fresh club. It is reachable only from the
 * Create sheet, and only for accounts holding `GAME_CREATOR`.
 */
@HiltViewModel
internal class CreateGameViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val gameRepository: GameRepository,
) : MviViewModel<CreateGameState, CreateGameEvent, Unit>() {

    private val _state = MutableStateFlow(restoreDraft())
    override val state: StateFlow<CreateGameState> = _state.asStateFlow()

    override fun onEvent(event: CreateGameEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            is OnNameChanged -> updateDraft { it.copy(name = event.name, formError = null) }
            is OnDescriptionChanged -> updateDraft { it.copy(description = event.description) }
            OnMinPlayersIncrement -> updateDraft { it.withMinPlayers(it.minPlayers + 1) }
            OnMinPlayersDecrement -> updateDraft { it.withMinPlayers(it.minPlayers - 1) }
            OnMaxPlayersIncrement -> updateDraft { it.withMaxPlayers(it.maxPlayers + 1) }
            OnMaxPlayersDecrement -> updateDraft { it.withMaxPlayers(it.maxPlayers - 1) }
            is OnResultTypeSelected -> updateDraft { it.copy(resultType = event.resultType) }
            OnSubmitClick -> submit()
        }
    }

    /** Raising the floor pushes the ceiling up with it, so the bounds can never cross. */
    private fun CreateGameState.withMinPlayers(value: Int): CreateGameState {
        val min = value.coerceIn(MIN_PLAYERS_LIMIT, MAX_PLAYERS_LIMIT)
        return copy(minPlayers = min, maxPlayers = maxPlayers.coerceAtLeast(min), formError = null)
    }

    /** Lowering the ceiling pulls the floor down with it, for the same reason. */
    private fun CreateGameState.withMaxPlayers(value: Int): CreateGameState {
        val max = value.coerceIn(MIN_PLAYERS_LIMIT, MAX_PLAYERS_LIMIT)
        return copy(maxPlayers = max, minPlayers = minPlayers.coerceAtMost(max), formError = null)
    }

    private fun submit() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(formError = StringResource(R.string.create_game_error_no_name)) }
            return
        }
        if (current.isSubmitting) return

        _state.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            val result = gameRepository.createGame(
                name = current.name.trim(),
                description = current.description.trim().takeIf(String::isNotBlank),
                minPlayers = current.minPlayers,
                maxPlayers = current.maxPlayers,
                resultType = current.resultType,
            ).firstSuccessOrErrorResult()

            _state.update { it.copy(isSubmitting = false) }
            if (result is Result.Error) {
                _state.update { it.copy(formError = result.error.toCreateGameError()) }
                return@launch
            }
            clearDraft()
            router.execute(
                ShowSnackbar(
                    SnackbarScreenConfig { message(StringResource(R.string.create_game_added)) }
                ),
                Back(),
            )
        }
    }

    private fun updateDraft(transform: (CreateGameState) -> CreateGameState) {
        _state.update { current ->
            val updated = transform(current)
            saveDraft(updated)
            updated
        }
    }

    private fun saveDraft(state: CreateGameState) {
        savedStateHandle[KEY_NAME] = state.name
        savedStateHandle[KEY_DESCRIPTION] = state.description
        savedStateHandle[KEY_MIN_PLAYERS] = state.minPlayers
        savedStateHandle[KEY_MAX_PLAYERS] = state.maxPlayers
        savedStateHandle[KEY_RESULT_TYPE] = state.resultType.name
    }

    private fun clearDraft() {
        listOf(KEY_NAME, KEY_DESCRIPTION, KEY_MIN_PLAYERS, KEY_MAX_PLAYERS, KEY_RESULT_TYPE)
            .forEach { key -> savedStateHandle.remove<Any>(key) }
    }

    private fun restoreDraft(): CreateGameState = CreateGameState(
        name = savedStateHandle[KEY_NAME] ?: "",
        description = savedStateHandle[KEY_DESCRIPTION] ?: "",
        minPlayers = savedStateHandle[KEY_MIN_PLAYERS] ?: CreateGameState.DEFAULT_MIN_PLAYERS,
        maxPlayers = savedStateHandle[KEY_MAX_PLAYERS] ?: CreateGameState.DEFAULT_MAX_PLAYERS,
        resultType = savedStateHandle.get<String>(KEY_RESULT_TYPE)
            ?.let { name -> GameResultType.entries.firstOrNull { it.name == name } }
            ?: GameResultType.POINTS,
    )

    private companion object {
        const val KEY_NAME = "draft_name"
        const val KEY_DESCRIPTION = "draft_description"
        const val KEY_MIN_PLAYERS = "draft_min_players"
        const val KEY_MAX_PLAYERS = "draft_max_players"
        const val KEY_RESULT_TYPE = "draft_result_type"
    }
}
