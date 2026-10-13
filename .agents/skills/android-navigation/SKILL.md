---
name: android-navigation-module
description: >
  Architecture rules and reference implementation for the project's navigation system:
  a Router/Screen/Command abstraction implemented with Jetpack Navigation 3 (NavDisplay,
  NavKey, developer-owned back stack), Jetpack Compose UI, Kotlin, and Dagger Hilt.
  ALWAYS use this skill when the task involves: creating or modifying the core navigation
  module; adding a new screen (any class named *Screen, ComposableScreen subclasses);
  navigating between screens (Forward, Replace, Back, ShowSnackbar or any navigation
  Command); passing arguments to a screen's ViewModel; saving ViewModel or navigation
  state across process death; wiring Router into feature modules; or any mention of
  "навигация", "роутер", "экран", NavDisplay, NavKey, NavBackStack, or Navigation 3.
  Do NOT use Navigation 2 (NavController/NavHost) patterns in this project.
---

# Android Navigation Module (Nav3 + Compose + Hilt)

This skill defines how navigation is built and used in this project. The architecture
is a Cicerone-style **Router + Command** pattern on top of **Jetpack Navigation 3**
(stable, `androidx.navigation3:*:1.0.0`), where the app owns the back stack as plain
Compose state.

> **Module location:** there is no separate `:core:navigation` module — navigation
> lives in `:core:presentation`, package
> `ru.gohasoft.wanderingtable.core.presentation.navigation` (merged so that
> `ShowSnackbar` can take `SnackbarScreenConfig` directly). Wherever this skill or
> its references say `:core:navigation`, read `:core:presentation`.

## Core abstractions (package `core.presentation.navigation`)

| Abstraction | Role |
|---|---|
| `Screen` | Marker interface for a navigation destination. Extends `NavKey`. |
| `ComposableScreen` | Abstract `@Serializable` class implementing `Screen`. Declares `@Composable fun getContent()`. Concrete screens (e.g. `ProfileScreen`) subclass it. |
| `Command` | A single operation over navigation state: `suspend fun execute(context: NavigationCommandContext)`. Implementable by ANY feature module. |
| `Router` | Entry point for navigation: `fun execute(vararg commands: Command)`. Injected into ViewModels. |
| `Navigation3Router` | `@Singleton` Router implementation. Buffers commands into a `Channel`; does NOT touch UI directly. |
| `NavigationCommandContext` | What commands operate on: the `NavBackStack` + snackbar access. Lives in the UI layer. |
| `NavigationHost` | Root composable: `rememberNavBackStack`, `NavDisplay`, snackbar host, and the command-collecting loop. |

## Non-negotiable rules

1. **Never use Navigation 2.** No `NavController`, `NavHost`, `composable()` routes,
   string routes, or `navigation-compose` dependency. Only Nav3 (`NavDisplay`,
   `NavKey`, `NavBackStack`, `entryProvider`).
2. **Screens are serializable data.** Every concrete screen is a `@Serializable`
   `data class` (with args) or `data object` (no args) extending `ComposableScreen`.
   Constructor parameters may contain ONLY serializable data (screen arguments).
   Never store dependencies, callbacks, ViewModels, or Compose state in a screen's
   constructor — screens are recreated from serialized state after process death.
3. **ViewModels never touch the back stack.** ViewModels navigate exclusively via
   `router.execute(...)`. Composables call ViewModel methods; only
   `NavigationHost` applies commands to the `NavBackStack`.
4. **Screen arguments go to the ViewModel via Hilt Assisted Injection**, not via
   `SavedStateHandle`. Nav3 does NOT auto-populate `SavedStateHandle` with route
   arguments (that was a Nav2 mechanism). Pattern:
   `@HiltViewModel(assistedFactory = ...)` + `@AssistedInject` + `@Assisted val screen: ProfileScreen`,
   obtained in `getContent()` via `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(this) })`.
5. **`SavedStateHandle` is still used — but only for process-death state**, never for
   reading navigation arguments. Inject it alongside the assisted screen argument.
6. **Process-death safety requires all three layers** (see reference below):
   `rememberNavBackStack(...)` for the stack itself, the entry decorators
   (`rememberSaveableStateHolderNavEntryDecorator()` + `rememberViewModelStoreNavEntryDecorator()`)
   for per-entry `rememberSaveable`/ViewModel scoping, and `SavedStateHandle` inside
   ViewModels for their own state.
7. **Feature modules depend only on `:core:presentation` abstractions.** A feature
   module may define its own `Command` implementations and its own screens without
   touching the core module. The core module must never depend on feature modules.
8. **UI events like snackbars are Commands too** — `ShowSnackbar(config: SnackbarScreenConfig)`.
   `Command.execute` is `suspend` precisely so commands can await snackbar display,
   animations, etc. The snackbar's text is a `TextResource`, resolved inside
   `NavigationHost`.
9. **Guard against double-clicks** on navigation triggers with `dropUnlessResumed`
   or equivalent when wiring click handlers that navigate.

## Workflow — pick the task, read the matching reference

- **Creating the core navigation module from scratch** (or fixing/refactoring it):
  read `references/core-module.md`. It contains the complete, copy-ready
  implementation of every core class, Hilt bindings, `NavigationHost`, and the
  Gradle dependencies (exact artifacts + kotlinx-serialization plugin setup).
- **Adding a new screen in a feature module** (screen + ViewModel + arguments +
  process-death state): read `references/feature-screens.md`. Follow it verbatim —
  it encodes the assisted-injection and SavedStateHandle pattern.
- **Adding a new Command** (core or feature-module custom command): read
  `references/custom-commands.md`.

When more than one applies (e.g. "set up navigation and add two screens"), read
`core-module.md` first, then the others.

## Quick sanity checklist before finishing any navigation task

- [ ] No Nav2 imports anywhere (`androidx.navigation.` without the `3`).
- [ ] Every new screen: `@Serializable`, extends `ComposableScreen`, args are
      serializable primitives/data classes.
- [ ] Navigation is triggered only through `Router`, from ViewModels.
- [ ] ViewModel with args uses `@AssistedInject` + factory + `creationCallback`.
- [ ] `SavedStateHandle` used for state persistence where the screen has
      user-modifiable transient state worth surviving process death.
- [ ] New feature commands implement `Command` from `:core:presentation` only.
- [ ] Build passes: kotlinx-serialization plugin applied to every module that
      declares screens.
