package ru.gohasoft.wanderingtable.core.presentation.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <T> Flow<T>.mutableStateIn(
    coroutineScope: CoroutineScope,
    initialValue: T,
): MutableStateFlow<T> {
    val state = MutableStateFlow(initialValue)
    coroutineScope.launch { collect(state) }
    return state
}