package ru.gohasoft.wanderingtable.feature.main.createrequest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnDateFieldClick
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnDatePicked
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnDatePickerDismissed
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnGameSelected
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnNoteChanged
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnPlayersDecrement
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnPlayersIncrement
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnSkillSelected
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnSubmitClick
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnTableFieldClick
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnTablePicked
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnTablePickerDismissed
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnTimeFieldClick
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnTimePicked
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestEvent.OnTimePickerDismissed
import ru.gohasoft.wanderingtable.feature.main.mapper.toCreateRequestError
import ru.gohasoft.wanderingtable.feature.main.mapper.toDateFieldLabel
import ru.gohasoft.wanderingtable.feature.main.mapper.toTimeFieldLabel
import ru.gohasoft.wanderingtable.feature.main.settings.WatchedGameUi

/**
 * Posts a "find an opponent" request, which the server models as a regular game event.
 *
 * The draft survives process death: the game, start time, seat count, note and the two UI-only
 * fields are all mirrored into [SavedStateHandle] as they change.
 */
@HiltViewModel
internal class CreateRequestViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val gameRepository: GameRepository,
    private val gameEventRepository: GameEventRepository,
) : MviViewModel<CreateRequestState, CreateRequestEvent, Unit>() {

    private var catalogue: List<Game> = emptyList()

    private val _state = MutableStateFlow(restoreDraft())
    override val state: StateFlow<CreateRequestState> = _state.asStateFlow()

    init {
        loadCatalogue()
    }

    override fun onEvent(event: CreateRequestEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            is OnGameSelected -> updateDraft { it.copy(selectedGameId = event.gameId, formError = null) }
            OnDateFieldClick -> _state.update { it.copy(isDatePickerVisible = true) }
            OnDatePickerDismissed -> _state.update { it.copy(isDatePickerVisible = false) }
            is OnDatePicked -> applyDate(event.epochMillis)
            OnTimeFieldClick -> _state.update { it.copy(isTimePickerVisible = true) }
            OnTimePickerDismissed -> _state.update { it.copy(isTimePickerVisible = false) }
            is OnTimePicked -> applyTime(event.hour, event.minute)
            OnPlayersIncrement -> updateDraft {
                it.copy(playersNeeded = (it.playersNeeded + 1).coerceAtMost(MAX_PLAYERS_NEEDED))
            }

            OnPlayersDecrement -> updateDraft {
                it.copy(playersNeeded = (it.playersNeeded - 1).coerceAtLeast(1))
            }

            is OnSkillSelected -> updateDraft { it.copy(skillLevel = event.skillLevel) }
            OnTableFieldClick -> _state.update { it.copy(isTablePickerVisible = true) }
            OnTablePickerDismissed -> _state.update { it.copy(isTablePickerVisible = false) }
            is OnTablePicked -> updateDraft {
                it.copy(table = event.table, isTablePickerVisible = false)
            }

            is OnNoteChanged -> updateDraft { it.copy(note = event.note) }
            OnSubmitClick -> submit()
        }
    }

    private fun loadCatalogue() {
        viewModelScope.launch {
            catalogue = gameRepository.getGames().firstSuccessOrErrorData().orEmpty()
            _state.update { current ->
                current.copy(
                    isLoading = false,
                    games = catalogue.map { WatchedGameUi(id = it.id, name = it.name) },
                )
            }
        }
    }

    private fun applyDate(epochMillis: Long) {
        val picked = Instant.ofEpochMilli(epochMillis).atZone(UTC).toLocalDate()
        val current = Instant.ofEpochMilli(_state.value.startsAtEpochMillis).atZone(zone)
        applyStartsAt(picked.atTime(current.toLocalTime()).atZone(zone).toInstant())
        _state.update { it.copy(isDatePickerVisible = false) }
    }

    private fun applyTime(hour: Int, minute: Int) {
        val current = Instant.ofEpochMilli(_state.value.startsAtEpochMillis).atZone(zone)
        val updated = current.toLocalDate().atTime(LocalTime.of(hour, minute)).atZone(zone)
        applyStartsAt(updated.toInstant())
        _state.update { it.copy(isTimePickerVisible = false) }
    }

    private fun applyStartsAt(instant: Instant) {
        updateDraft { current ->
            current.copy(
                startsAtEpochMillis = instant.toEpochMilli(),
                dateLabel = instant.toDateFieldLabel(zone),
                timeLabel = instant.toTimeFieldLabel(zone),
            )
        }
    }

    private fun submit() {
        val current = _state.value
        val gameId = current.selectedGameId ?: run {
            _state.update { it.copy(formError = StringResource(R.string.create_request_error_no_game)) }
            return
        }
        if (current.isSubmitting) return

        val game = catalogue.firstOrNull { it.id == gameId }
        val maxParticipants = current.playersNeeded + 1
        val minParticipants = MIN_PARTICIPANTS.coerceAtMost(maxParticipants)
        // Checked here as well as server-side, so an impossible seat count is caught before the
        // round trip rather than coming back as an opaque 400.
        if (game != null && !game.allows(minParticipants, maxParticipants)) {
            _state.update {
                it.copy(
                    formError = StringResource(
                        R.string.create_request_error_player_bounds,
                        listOf(
                            game.minPlayers?.toString().orEmpty(),
                            game.maxPlayers?.toString().orEmpty(),
                        ),
                    )
                )
            }
            return
        }

        _state.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            val result = gameEventRepository.createRegularGame(
                gameId = gameId,
                title = game?.name ?: current.games.firstOrNull { it.id == gameId }?.name.orEmpty(),
                description = current.note.takeIf(String::isNotBlank),
                startsAt = Instant.ofEpochMilli(current.startsAtEpochMillis),
                durationMinutes = DEFAULT_DURATION_MINUTES,
                minParticipants = minParticipants,
                maxParticipants = maxParticipants,
            ).firstSuccessOrErrorResult()

            _state.update { it.copy(isSubmitting = false) }
            if (result is Result.Error) {
                _state.update { it.copy(formError = result.error.toCreateRequestError()) }
                return@launch
            }
            clearDraft()
            router.execute(
                ShowSnackbar(
                    SnackbarScreenConfig { message(StringResource(R.string.create_request_posted)) }
                ),
                Back(),
            )
        }
    }

    private fun Game.allows(minParticipants: Int, maxParticipants: Int): Boolean {
        val lowerBound = minPlayers ?: return true
        val upperBound = maxPlayers ?: return true
        return minParticipants >= lowerBound && maxParticipants <= upperBound
    }

    private fun updateDraft(transform: (CreateRequestState) -> CreateRequestState) {
        _state.update { current ->
            val updated = transform(current)
            saveDraft(updated)
            updated
        }
    }

    private fun saveDraft(state: CreateRequestState) {
        savedStateHandle[KEY_GAME_ID] = state.selectedGameId
        savedStateHandle[KEY_STARTS_AT] = state.startsAtEpochMillis
        savedStateHandle[KEY_PLAYERS] = state.playersNeeded
        savedStateHandle[KEY_SKILL] = state.skillLevel.name
        savedStateHandle[KEY_TABLE] = state.table
        savedStateHandle[KEY_NOTE] = state.note
    }

    private fun clearDraft() {
        listOf(KEY_GAME_ID, KEY_STARTS_AT, KEY_PLAYERS, KEY_SKILL, KEY_TABLE, KEY_NOTE)
            .forEach { key -> savedStateHandle.remove<Any>(key) }
    }

    private fun restoreDraft(): CreateRequestState {
        val startsAt = savedStateHandle.get<Long>(KEY_STARTS_AT)?.takeIf { it > 0L }
            ?: defaultStartsAt().toEpochMilli()
        val instant = Instant.ofEpochMilli(startsAt)
        return CreateRequestState(
            selectedGameId = savedStateHandle[KEY_GAME_ID],
            startsAtEpochMillis = startsAt,
            dateLabel = instant.toDateFieldLabel(zone),
            timeLabel = instant.toTimeFieldLabel(zone),
            playersNeeded = savedStateHandle[KEY_PLAYERS] ?: 1,
            skillLevel = savedStateHandle.get<String>(KEY_SKILL)
                ?.let { name -> SkillLevelUi.entries.firstOrNull { it.name == name } }
                ?: SkillLevelUi.ANY,
            table = savedStateHandle[KEY_TABLE] ?: DEFAULT_TABLE,
            note = savedStateHandle[KEY_NOTE] ?: "",
        )
    }

    /** Tomorrow evening: the club's usual slot, and always a valid future start. */
    private fun defaultStartsAt(): Instant = LocalDate.now(zone)
        .plusDays(1)
        .atTime(DEFAULT_HOUR, 0)
        .atZone(zone)
        .toInstant()

    private val zone: ZoneId get() = ZoneId.systemDefault()

    private companion object {
        /** The Material date picker reports the selected day as UTC midnight. */
        val UTC: ZoneId = ZoneId.of("UTC")

        const val MIN_PARTICIPANTS = 2
        const val MAX_PLAYERS_NEEDED = 11
        const val DEFAULT_HOUR = 19

        /** The design has no duration field, and the server requires one. */
        const val DEFAULT_DURATION_MINUTES = 90
        const val DEFAULT_TABLE = "Table 1"

        const val KEY_GAME_ID = "draft_game_id"
        const val KEY_STARTS_AT = "draft_starts_at"
        const val KEY_PLAYERS = "draft_players"
        const val KEY_SKILL = "draft_skill"
        const val KEY_TABLE = "draft_table"
        const val KEY_NOTE = "draft_note"
    }
}
