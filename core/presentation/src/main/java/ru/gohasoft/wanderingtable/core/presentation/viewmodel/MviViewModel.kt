package ru.gohasoft.wanderingtable.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result

/**
 * Base MVI ViewModel: a single [state], user-triggered [onEvent]s, and one-time
 * [effects] delivered exactly once (navigation, dialogs, etc.).
 */
abstract class MviViewModel<State, Event, Effect> : ViewModel(), EventHandler<Event> {

    abstract val state: StateFlow<State>

    private val _effects = Channel<Effect>()
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    override fun onEvent(event: Event) = Unit

    protected fun emitEffect(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

interface EventHandler<Event> {
    fun onEvent(event: Event)
}