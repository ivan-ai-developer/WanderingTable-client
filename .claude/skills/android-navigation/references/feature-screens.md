# Adding a screen in a feature module

Follow this pattern verbatim for every new screen. Example: `ProfileScreen`
with a `userId` argument in module `:feature:profile`.

## 0. Module prerequisites

`:feature:profile` `build.gradle.kts`:
- apply `kotlin("plugin.serialization")` — REQUIRED, screens are `@Serializable`;
- depend on `:core:presentation` (brings navigation + configs), Hilt (+ ksp compiler), `androidx.hilt:hilt-navigation-compose`.

## 1. The screen — serializable key + UI

```kotlin
package com.example.feature.profile

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel // NOT androidx.hilt.navigation.compose — that one is deprecated
import com.example.core.navigation.ComposableScreen
import kotlinx.serialization.Serializable

@Serializable
data class ProfileScreen(val userId: String) : ComposableScreen() {

    @Composable
    override fun Content() {
        // Screen args flow into the VM through the assisted factory.
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory>(
            creationCallback = { factory -> factory.create(this) }
        )
        ProfileContent(viewModel)
    }
}
```

For a screen without arguments use `@Serializable data object SettingsScreen : ComposableScreen() { ... }`.

Rules:
- Constructor params: serializable primitives / `@Serializable` data classes only.
  Pass IDs, not domain objects — the ViewModel loads data by ID.
- NO `val viewModel`, `lateinit`, lambdas, or injected fields in the class body.
  The screen instance is recreated by deserialization after process death;
  anything outside constructor args is lost or breaks serialization.
- The actual UI goes in a separate top-level `@Composable fun ProfileContent(...)`
  for previewability; `Content()` only wires the VM.

## 2. The ViewModel — assisted args + SavedStateHandle for process death

```kotlin
package com.example.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.core.navigation.Router
import com.example.core.navigation.commands.Back
import com.example.core.navigation.commands.Forward
import com.example.core.navigation.commands.ShowSnackbar
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted private val screen: ProfileScreen,   // navigation arguments
    private val savedStateHandle: SavedStateHandle, // process-death state ONLY
    private val router: Router,
    private val repository: ProfileRepository,
) : MviViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(screen: ProfileScreen): ProfileViewModel
    }

    val userId: String get() = screen.userId

    // Transient user-editable state that must survive process death:
    val draftBio = savedStateHandle.getStateFlow(KEY_DRAFT_BIO, "")
    
    override fun onEvent(event: Event) {
        when (event) {
            is OnBioChanged -> saveBio(event.value)
            is OnProfileCardClick -> navigateToProfileScreen(event.friendId)
            OnSaved -> showSuccessfulSavingSnackbar()
        }
    }

    private fun saveBio(value: String) {
        savedStateHandle[KEY_DRAFT_BIO] = value
    }

    private fun navigateToProfileScreen(friendId: String) {
        router.execute(Forward(ProfileScreen(userId = friendId)))
    }

    private fun showSuccessfulSavingSnackbar() {
        router.execute(ShowSnackbar("Профиль сохранён"), Back())
    }

    private companion object { const val KEY_DRAFT_BIO = "draft_bio" }
}
```

Critical points (do NOT deviate):
- **Never read navigation arguments from `SavedStateHandle`.** Nav3 does not
  populate it with route args (that was Nav2 behavior); reading args from it
  crashes or yields nulls. Args come only through `@Assisted`.
- `SavedStateHandle` IS still injected and IS the mechanism for persisting VM
  state across process death (works because `rememberSaveableStateHolderNavEntryDecorator`
  + `rememberViewModelStoreNavEntryDecorator` are installed in `NavigationHost`).
  Use `getStateFlow`/`set` for anything the user typed or scrolled that should
  survive the app being killed in background.
- The ViewModel navigates ONLY via `router.execute(...)`. It never sees the
  back stack, `NavigationHost`, or any Compose type.
- Each screen entry gets its own ViewModel instance, cleared when the screen is
  popped. Two `ProfileScreen`s on the stack = two independent ViewModels.

## 3. The UI composable

```kotlin
@Composable
internal fun ProfileContent(viewModel: ProfileViewModel) {
    val draftBio by viewModel.draftBio.collectAsStateWithLifecycle()
    // ... UI; click handlers call viewModel methods.
    // Guard navigation clicks against double-taps:
    Button(onClick = dropUnlessResumed { viewModel.onFriendClicked(friend.id) }) { ... }
}
```

## 4. Navigating TO this screen from another feature

The caller only needs a dependency on the screen's public API:

```kotlin
router.execute(Forward(ProfileScreen(userId = "42")))
```

If features must not depend on each other directly, expose the screen from a
small `:feature:profile:api` module containing only the `@Serializable` screen
class (its `Content()` can delegate to an interface bound via Hilt), or route
через a shared `Destinations` module. Prefer the simple direct dependency unless
the project already enforces api/impl module split.

## 5. Checklist for every new screen

- [ ] `@Serializable`, extends `ComposableScreen`, serializable ctor args only.
- [ ] `kotlin("plugin.serialization")` applied in the module.
- [ ] VM: `@HiltViewModel(assistedFactory=...)` + `@AssistedInject` +
      `@Assisted screen` + `@AssistedFactory`.
- [ ] `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(this) })`
      inside `Content()`.
- [ ] Transient editable state persisted через `SavedStateHandle`.
- [ ] Navigation only via `Router`.
- [ ] Manual test: background app → `adb shell am kill <pkg>` → reopen →
      screen, args, and draft state restored.
