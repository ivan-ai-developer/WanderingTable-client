package ru.gohasoft.wanderingtable.core.presentation.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects one-time effects only while the lifecycle is at least STARTED, on
 * Main.immediate so an effect emitted right before a config change is not lost.
 */
@Composable
fun <T> ObserveEffects(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEffect: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle, key1, key2, flow) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEffect)
            }
        }
    }
}

@Composable
fun <State, Event, Effect> MviContent(
    viewModel: MviViewModel<State, Event, Effect>,
    onEffect: suspend (Effect) -> Unit = {},
    content: @Composable EventHandler<Event>.(State) -> Unit,
) {
    ObserveEffects(viewModel.effects, onEffect = onEffect)
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.content(state)
}