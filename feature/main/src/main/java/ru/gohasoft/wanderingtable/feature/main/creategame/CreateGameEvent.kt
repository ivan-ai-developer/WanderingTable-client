package ru.gohasoft.wanderingtable.feature.main.creategame

import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType

internal sealed interface CreateGameEvent {
    data object OnBackClick : CreateGameEvent
    data class OnNameChanged(val name: String) : CreateGameEvent
    data class OnDescriptionChanged(val description: String) : CreateGameEvent
    data object OnMinPlayersIncrement : CreateGameEvent
    data object OnMinPlayersDecrement : CreateGameEvent
    data object OnMaxPlayersIncrement : CreateGameEvent
    data object OnMaxPlayersDecrement : CreateGameEvent
    data class OnResultTypeSelected(val resultType: GameResultType) : CreateGameEvent
    data object OnSubmitClick : CreateGameEvent
}
