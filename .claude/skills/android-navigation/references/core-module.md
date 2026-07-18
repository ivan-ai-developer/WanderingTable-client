# Core navigation module (`:core:navigation`) — full implementation

Complete reference implementation. Adapt package names to the project
(`com.example.core.navigation` used below). Keep class names as-is.

## 1. Gradle setup

Version catalog (`gradle/libs.versions.toml`):

```toml
[versions]
nav3 = "1.0.0"                     # check for the latest stable
lifecycleNav3 = "1.0.0"            # lifecycle-viewmodel-navigation3
hilt = "2.57"
hiltNavigationCompose = "1.3.0"
kotlinxSerialization = "1.9.0"

[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "nav3" }
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "nav3" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycleNav3" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinxSerialization" }
```

`:core:navigation` `build.gradle.kts` essentials:

```kotlin
plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")      // REQUIRED in every module that declares screens
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    api(libs.kotlinx.serialization.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    api(libs.androidx.hilt.navigation.compose)
    // + compose BOM, material3, activity-compose as usual
}
```

The kotlinx-serialization plugin must also be applied in every **feature module**
that defines `ComposableScreen` subclasses.

## 2. Screen abstractions

```kotlin
package com.example.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** A navigation destination. Extends NavKey so it can live in a NavBackStack
 *  and be persisted across process death via rememberNavBackStack. */
interface Screen

/** Base class for all Compose screens. Concrete screens MUST be @Serializable
 *  data classes / data objects whose constructor holds only serializable args. */
@Serializable
abstract class ComposableScreen : Screen, NavKey {

    /** The screen's UI. Obtain the ViewModel INSIDE this method via hiltViewModel;
     *  never store it in the class. */
    @Composable
    abstract fun Content()
}
```

Why this works: Nav3's `rememberNavBackStack` serializes `NavKey`s with
kotlinx-serialization (storing the concrete class), so screens restore after
process death — including their constructor arguments. Methods (like
`Content`) are irrelevant to serialization; only constructor/property state
matters, which is why rule #2 in SKILL.md forbids non-data state in screens.

## 3. Command abstraction

```kotlin
package com.example.core.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.navigation3.runtime.NavBackStack

/** Everything a Command may operate on. Provided by NavigationHost. */
interface NavigationCommandContext {
    /** The single source of truth for navigation state (developer-owned in Nav3). */
    val backStack: NavBackStack<Screen>

    /** Suspends until the snackbar is dismissed/actioned. */
    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ): SnackbarResult
}

/** A single navigation operation. Feature modules may implement their own. */
fun interface Command {
    suspend fun execute(context: NavigationCommandContext)
}
```

## 4. Built-in commands

```kotlin
package com.example.core.navigation.commands

import com.example.core.navigation.Command
import com.example.core.navigation.NavigationCommandContext
import com.example.core.navigation.Screen

/** Push a screen on top of the stack. */
class Forward(private val screen: Screen) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        context.backStack.add(screen)
    }
}

/** Replace the topmost screen. */
class Replace(private val screen: Screen) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        with(context.backStack) {
            removeLastOrNull()
            add(screen)
        }
    }
}

/** Pop the topmost screen. No-op guard: never empties the stack completely. */
class Back : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        if (context.backStack.size > 1) context.backStack.removeLastOrNull()
    }
}

/** Pop until the given screen type is on top (or no-op if absent). */
class BackTo(private val screenClass: Class<out Screen>) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        val stack = context.backStack
        val index = stack.indexOfLast { screenClass.isInstance(it) }
        if (index >= 0) {
            while (stack.size > index + 1) stack.removeLastOrNull()
        }
    }
}

/** Clear the stack and start from a new root. */
class NewRoot(private val screen: Screen) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        with(context.backStack) {
            clear()
            add(screen)
        }
    }
}

/** Show a snackbar. Suspends until dismissed, so a subsequent command in the
 *  same execute(...) batch runs after it. */
class ShowSnackbar(
    private val message: String,
    private val actionLabel: String? = null,
    private val onAction: (suspend (NavigationCommandContext) -> Unit)? = null,
) : Command {
    override suspend fun execute(context: NavigationCommandContext) {
        val result = context.showSnackbar(message, actionLabel)
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            onAction?.invoke(context)
        }
    }
}
```

Convenience: `inline fun <reified S : Screen> BackTo() = BackTo(S::class.java)`.

## 5. Router

```kotlin
package com.example.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

interface Router {
    /** Commands in one call run sequentially as a batch. */
    fun execute(vararg commands: Command)
}

@Singleton
class Navigation3Router @Inject constructor() : Router {

    // UNLIMITED buffer: commands sent before the UI attaches (or during
    // configuration change) are queued, not dropped.
    private val commandChannel = Channel<List<Command>>(Channel.UNLIMITED)

    /** Collected by NavigationHost only. */
    val commands: Flow<List<Command>> = commandChannel.receiveAsFlow()

    override fun execute(vararg commands: Command) {
        commandChannel.trySend(commands.toList())
    }
}
```

Hilt binding:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface NavigationModule {
    @Binds
    fun bindRouter(impl: Navigation3Router): Router
}
```

Design note: the Router is a singleton and the `NavBackStack` lives inside
composition (`rememberNavBackStack`), so the Router must NOT hold the stack.
It only emits commands; `NavigationHost` owns the stack and applies them.
This keeps ViewModels free of any Compose/UI dependency.

## 6. NavigationHost (root composable)

```kotlin
package com.example.core.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

@Composable
fun NavigationHost(
    router: Navigation3Router,
    startScreen: ComposableScreen,
) {
    // Survives process death: NavKey + @Serializable screens make this possible.
    val backStack = rememberNavBackStack<Screen>(startScreen)
    val snackbarHostState = remember { SnackbarHostState() }

    val commandContext = remember(backStack, snackbarHostState) {
        object : NavigationCommandContext {
            override val backStack = backStack
            override suspend fun showSnackbar(
                message: String,
                actionLabel: String?,
                duration: SnackbarDuration,
            ): SnackbarResult =
                snackbarHostState.showSnackbar(message, actionLabel, duration = duration)
        }
    }

    // Apply commands sequentially on the UI scope.
    LaunchedEffect(router, commandContext) {
        router.commands.collect { batch ->
            batch.forEach { command -> command.execute(commandContext) }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
            onBack = { router.execute(com.example.core.navigation.commands.Back()) },
            entryDecorators = listOf(
                // Order matters. Both are REQUIRED for process-death safety:
                // 1) per-entry rememberSaveable persistence,
                // 2) per-entry ViewModelStore (each screen gets its own VM,
                //    cleared when popped).
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                // Screens carry their own UI, so no central route registry is
                // needed: feature modules add screens with zero changes here.
                NavEntry(key) { (key as ComposableScreen).Content() }
            },
        )
    }
}
```

Activity wiring:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var router: Navigation3Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme { NavigationHost(router = router, startScreen = HomeScreen) }
        }
    }
}
```

## 7. Verification steps after implementing

1. Compile: kotlinx-serialization plugin present wherever screens are declared.
2. Navigate forward/back, rotate the device — stack survives.
3. Kill the process (`adb shell am kill <package>` while backgrounded), reopen —
   the same screen with the same arguments is restored.
4. `ShowSnackbar` followed by `Back` in one `execute(...)` call: snackbar is
   shown first, back happens after dismissal (suspend semantics).
