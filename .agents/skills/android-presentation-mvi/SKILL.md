---
name: android-presentation-mvi
description: |
  MVI presentation layer for Android - State, Action, Event, ViewModel, Screen composable, UI models, TextResource error mapping, and process death with SavedStateHandle. Use this skill whenever creating or reviewing a ViewModel, defining screen state, actions, or events, structuring composables, mapping errors to UI strings, or handling process death. Trigger on phrases like "add a ViewModel", "create a screen", "MVI", "state", "action", "event", "screen composable", "TextResource", "SavedStateHandle", "ObserveAsEvents", or "UI model".
---
 
# Android Presentation Layer (MVI)
 
## Overview
 
Every screen has:
1. **State** — a single data class holding all UI state fields.
2. **Event** (Intent) — a sealed interface of all user-triggered events.
3. **Effect** — a sealed interface of one-time side effects (navigation, snackbar).
4. **ViewModel** — holds `StateFlow<State>`, processes `Action`, emits `Event` via `Channel`.
 
---
 
## State
 
```kotlin
data class NoteListState(
    val notes: List<NoteUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: TextResource? = null
)
```
 
Always update state with `.update { }` — never replace the entire flow:
```kotlin
_state.update { it.copy(isLoading = true) }
```
 
---
 
## Event (Intent)
 
```kotlin
sealed interface NoteListEvent {
    data object OnRefreshClick : NoteListEvent
    data class OnNoteClick(val noteId: String) : NoteListEvent
    data class OnDeleteNote(val noteId: String) : NoteListEvent
}
```
 
---
 
## Effect (one-time side effects)
 
```kotlin
sealed interface NoteListEffect {
    data class ShowSelectPopUpDialog(val noteId: String) : NoteListEffect
    data class ShowInformer(val message: TextResource) : NoteListEffect
}
```
 
---
 
## ViewModel

```kotlin

abstract class MviViewModel {
    private val _effects = Channel<Effect>()
    val effects = _effects.receiveAsFlow()
    open fun onEvent(event: Event) { ... }
    protected fun emitEffect(effect: Effect) { ... }
}

@HiltViewModel(assistedFactory = NoteListViewModel.Factory::class)
internal class NoteListViewModel (
    @Assisted private val screen: NoteListScreen,
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val noteRepository: NoteRepository
) : MviViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(screen: NoteListScreen): NoteListViewModel
    }

    private val refresher = MutableSateFlow(Refresher())
    val state = refresher.flatMapLatest { noteRepository.getNotes(forceRefresh = true) }
        .map { result ->
            when (result) {
                is Result.Error ->
                    NoteListState(
                        error = result.error.toTextResource(),
                        isLoading = false
                    )
                else -> {
                    NoteListState(
                        notes = result.data.map { it.toNoteUi() },
                        isLoading = result.isLoading
                    )
                }
            }
        }
        .withErrorHandling(mainStratagy = ShowErrorSnackbar())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NoteListState())

    override fun onEvent(event: Event) {
        when (event) {
            is NoteListAction -> when (event) {
                OnRefreshClick -> reloadNotes()
                OnNoteClick -> navigateToDetail(event.noteId)
            }
            else -> super.onEvent(event)
        }
    }
    
    private fun navigateToDetail(noteId: String) {
        router.execute(Forward(NoteDetailScreen(noteId)))
    }

    private fun reloadNotes() {
        refresher.value = Refresher()
    }
    
    private class Refresher
}
```
 
---
 
## Coroutine Dispatchers
 
**Do not inject** unless the class is unit-tested and dispatches to a non-main dispatcher. For ViewModel tests, use `Dispatchers.setMain(UnconfinedTestDispatcher())` in test setup.
 
For blocking code that doesn't support suspension, wrap it:
```kotlin
suspend fun compressImage(bytes: ByteArray): ByteArray = withContext(Dispatchers.IO) {
    // blocking compression logic
}
```
 
Only inject `CoroutineDispatcher` when:
1. The class dispatches to a non-main dispatcher (e.g., `IO`), AND
2. That class is directly unit-tested.
 
---
 
## Mapping Errors to UI Strings

`TextResource` (`core:presentation`) wraps strings that originate from — or could originate from — a string resource:

```kotlin
sealed interface TextResource {
    data class DynamicString(val value: String) : TextResource
    class StringResource(val id: Int, val args: Array<Any> = emptyArray()) : TextResource
}
```

**When to use `TextResource`:** For any string that comes from a string resource, could be localized, or might be either a resource or a dynamic value depending on context (e.g., error messages that map to `R.string.*`).

**When to use plain `String`:** For values that are always dynamic and never come from resources — e.g., a user's name, a formatted date, a currency amount. These should be exposed as `String` directly in the state or UI model.

```kotlin
// TextResource — error message that maps to a string resource
data class NoteListState(
    val error: TextResource? = null
)

// Plain String — always dynamic, never a resource
data class NoteUi(
    val authorName: String,
    val formattedDate: String
)
```

Define `DataError.toTextResource()` extension functions in `core:presentation` (or feature `presentation`) that map error enums to `TextResource.StringResource`.
 
---
 
## UI Model (Presentation Model)
 
When a domain model needs UI-specific formatting (dates, units, currency), create a dedicated UI model in the presentation layer:
 
```kotlin
data class NoteUi(
    val id: String,
    val title: String,
    val formattedDate: String  // e.g. "Mar 15, 2026"
)
 
fun Note.toNoteUi(): NoteUi = NoteUi(
    id = id,
    title = title,
    formattedDate = date.format(...)
)
```
 
UI models are always suffixed with `Ui` (e.g., `NoteUi`, `TodoItemUi`).
 
---
 
## Composable Structure

Both the Content and Screen composable live in the **same file** (e.g., `NoteListScreen.kt`).

```kotlin
// NoteListScreen.kt — Root + Screen in a single file

@Serializable
data class NoteListScreen(val id: String) : ComposableScreen() {

    @Composable
    override fun Content() {
        // Screen args flow into the VM through the assisted factory.
        val viewModel = hiltViewModel<NoteListViewModel, NoteListViewModel.Factory>(
            creationCallback = { factory -> factory.create(this) }
        )
        val state = viewModel.state.collectAsStateWithLifecycle()
        NoteListContent(state = NoteListState(), onEvent = viewModel:onEvent)
    }
}

@Preview
@Composable
private fun NoteListContentPreview() {
    NoteListContent(state = NoteListState(), onEvent = {})
}
```
 
---
 
## Process Death
 
When a screen involves complex forms or critical user input, restore essential fields using `SavedStateHandle`:
 
```kotlin
@HiltViewModel
class NoteEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        NoteEditorState(
            title = savedStateHandle["title"] ?: "",
            body = savedStateHandle["body"] ?: ""
        )
    )
 
    fun onEvent(event: Event) {
        when (event) {
            is NoteEditorAction.OnTitleChange -> 
                saveTitle(event.title)
        }
    }
    
    private fun saveTitle(title: String) {
        savedStateHandle["title"] = title
        _state.update { it.copy(title = action.title) }
    }
}
```
 
Only save what truly matters after process death — not the entire state.
 
---
 
## Naming Conventions
 
| Thing                        | Convention          | Example |
|------------------------------|---------------------|---|
| ViewModel                    | `<Screen>ViewModel` | `NoteListViewModel` |
| State                        | `<Screen>State`     | `NoteListState` |
| Event                        | `<Screen>Event`     | `NoteListEvent` |
| Effect                       | `<Screen>Effect`    | `NoteListEffect` |
| Navigation composable Screen | `<Screen>Screen`    | `NoteListScreen` |
| UI model                     | `<Model>Ui`         | `NoteUi`, `TodoItemUi` |
 
---
 
## Checklist: Adding a New Screen
 
- [ ] Define `State`, `Effect`, `Event` in `feature:presentation`
- [ ] Implement `ViewModel` in `feature:presentation`
- [ ] Create `<Screen>Screen` for navigation 
- [ ] Create `Content` in `<Screen>Screen` composable (holds ViewModel, observes effects, pure state + onEvent, previewable)
- [ ] Map any domain errors to `TextResource` via extension functions
- [ ] Add `SavedStateHandle` for any form fields that must survive process death