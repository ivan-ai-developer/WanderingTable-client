package ru.gohasoft.wanderingtable.feature.main.createrequest

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.main.settings.WatchedGameUi

/**
 * The "Find an Opponent" form.
 *
 * [skillLevel] and [table] are captured and validated locally but never posted — the server has
 * no field for either on a regular play. See [SkillLevelUi].
 */
internal data class CreateRequestState(
    val isLoading: Boolean = true,
    val games: List<WatchedGameUi> = emptyList(),
    val selectedGameId: String? = null,
    /** Wall-clock start, held as epoch millis so it survives process death in one key. */
    val startsAtEpochMillis: Long = 0L,
    val dateLabel: String = "",
    val timeLabel: String = "",
    val playersNeeded: Int = 1,
    val skillLevel: SkillLevelUi = SkillLevelUi.ANY,
    val table: String = "",
    val note: String = "",
    val isDatePickerVisible: Boolean = false,
    val isTimePickerVisible: Boolean = false,
    val isTablePickerVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val formError: TextResource? = null,
) {
    val canSubmit: Boolean get() = selectedGameId != null && !isSubmitting
}
