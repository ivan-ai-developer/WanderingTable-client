# Custom Commands (core and feature modules)

`Command` is deliberately minimal so any feature module can add navigation
behavior without modifying `:core:navigation`:

```kotlin
fun interface Command {
    suspend fun execute(context: NavigationCommandContext)
}
```

A command may only use what `NavigationCommandContext` exposes: the
`NavBackStack<Screen>` and `showSnackbar`. That keeps commands pure state
operations — testable without Compose or Android.

## Example 1: feature-module command — pop the whole auth flow after login

```kotlin
package com.example.feature.auth

import com.example.core.navigation.Command
import com.example.core.navigation.NavigationCommandContext

/** Removes every auth-flow screen and lands on MainScreen. */
class FinishAuthFlow : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        context.backStack.removeAll { it is AuthFlowScreen } // marker interface of the feature
        if (context.backStack.isEmpty()) context.backStack.add(MainScreen)
    }
}
```

Usage from the feature's ViewModel: `router.execute(FinishAuthFlow())`.

## Example 2: composite command with snackbar + undo

```kotlin
class DeleteWithUndo(
    private val message: String,
    private val onUndo: suspend (NavigationCommandContext) -> Unit,
) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        val result = context.showSnackbar(message, actionLabel = "Отменить")
        if (result == SnackbarResult.ActionPerformed) onUndo(context)
    }
}
```

## Example 3: conditional navigation

```kotlin
class ForwardIfAbsent(private val screen: Screen) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        if (context.backStack.none { it::class == screen::class }) {
            context.backStack.add(screen)
        }
    }
}
```

## Rules

1. Commands mutate ONLY through `NavigationCommandContext`. Never capture
   Activity, Context, NavDisplay, or SnackbarHostState in a command.
2. Commands are cheap, short-lived objects — no DI into commands. If a command
   needs data, pass it through the constructor at the call site (the ViewModel
   already has injected dependencies).
3. Long-running work does NOT belong in commands. Load data in the ViewModel,
   then execute a command with the result. `suspend` in `execute` exists for UI
   waiting (snackbar result, sequential batches), not for network calls.
4. Batches are sequential: `router.execute(a, b, c)` runs a → b → c, awaiting
   each. Use this for "show snackbar, then navigate" flows.
5. Never leave the back stack empty (except momentarily inside `NewRoot`-style
   commands that immediately add a root). An empty stack renders nothing.
6. If a command needs new capabilities (e.g. bottom-sheet host, dialogs), extend
   `NavigationCommandContext` in `:core:navigation` and implement it in
   `NavigationHost` — do not create feature-local контексты.

## Testing a command

Commands are unit-testable with a fake context:

```kotlin
class FakeContext(
    override val backStack: NavBackStack<Screen>,
) : NavigationCommandContext {
    val shownSnackbars = mutableListOf<String>()
    override suspend fun showSnackbar(message: String, actionLabel: String?, duration: SnackbarDuration): SnackbarResult {
        shownSnackbars += message
        return SnackbarResult.Dismissed
    }
}
```

(Construct `NavBackStack` directly in tests, or abstract it behind
`MutableList<Screen>` in the context interface if pure-JVM tests are required.)
