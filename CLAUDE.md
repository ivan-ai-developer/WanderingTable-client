# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

**Wandering Table** — a tabletop board game club app. The core layer is implemented (uikit, domain, data, presentation with navigation, Hilt DI, Application class); feature screens are not built yet. The app currently starts on a temporary test Welcome screen (`app/.../welcome/WelcomeScreen.kt`) that exercises the Router → Command → Snackbar pipeline — replace it when the first real feature lands.

The intended feature set can be inferred from the design mockups in `specs/` (`Wandering Table App Design.html` and `Wandering Table UI Kit.html`, bundled Figma-style exports — large single-file HTML, not meant to be edited by hand). They describe a club app for finding opponents and hosting board game sessions: sign up / log in, browse a game library (e.g. Chess, Azul, Wingspan, Settlers of Catan, Terraforming Mars), post/browse "Find an Opponent" requests with skill level and location, host/join games, club news and announcements, notifications, and a user profile with stats (wins, skill level).

When implementing new features, treat the design HTML files as the source of truth for screen names, copy, and flows, but do not attempt to parse or programmatically extract from them beyond visual/text reference.

## Memory Tools (MCP)

- Always prioritize `codebase-memory` tools (`trace_call_path`, `query_graph`, `get_architecture`) over global text search (`grep`).
- Use the knowledge graph to understand structural relations, call hierarchies, and architecture before modifying files. This saves context tokens.
- After major changes (changes caused by the /plan-mode command) you can update Claude.md.

## Build system

- Gradle (Kotlin DSL), Android Gradle Plugin 9.2.1, Kotlin 2.2.10, convention plugins in `build-logic/` (`wanderingtable.android.application`, `.android.library`, `.compose`, `.domain.module`, `.hilt`).
- Modules: `:app`, `:core:domain` (pure Kotlin), `:core:data` (pure Kotlin), `:core:presentation` (MVI + navigation + configs), `:core:uikit`.
- `compileSdk`/`targetSdk` 36, `minSdk` 28.
- UI toolkit: Jetpack Compose (Material 3), via the `androidx.compose.bom` (2026.02.01) and the `org.jetbrains.kotlin.plugin.compose` plugin — no XML layouts.
- DI: Dagger Hilt (KSP) via the `wanderingtable.hilt` convention plugin.
- **Built-in Kotlin is opted out** (`android.builtInKotlin=false` + `android.newDsl=false` in `gradle.properties`): AGP 9's built-in Kotlin silently disables KGP compiler plugins, and the project needs `kotlin-parcelize`. The conventions apply classic `org.jetbrains.kotlin.android`.
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog); add new dependencies there rather than hardcoding versions in `app/build.gradle.kts`.
- Note (Windows, this machine): `gradlew.bat` fails under the system Java 15; run Gradle as `java -jar gradle\wrapper\gradle-wrapper.jar -p <repo-root> <task>`.

## Common commands

Run from the repo root using the Gradle wrapper (`gradlew.bat` on Windows, `./gradlew` in POSIX shells):

```
gradlew assembleDebug              # Build debug APK
gradlew installDebug                # Build and install debug APK on a connected device/emulator
gradlew test                        # Run local unit tests (JVM, app/src/test)
gradlew testDebugUnitTest --tests "ru.gohasoft.wanderingtable.ExampleUnitTest"   # Run a single unit test class
gradlew connectedAndroidTest        # Run instrumented tests on a connected device/emulator (app/src/androidTest)
gradlew lint                        # Run Android Lint
```

There are currently no linters/formatters configured beyond Android Lint and `kotlin.code.style=official` (set in `gradle.properties`).

## Architecture notes

- Entry point: `WanderingTableApp` (`@HiltAndroidApp`) → `MainActivity` (`@AndroidEntryPoint`), which injects `Navigation3Router` and renders `NavigationHost(router, startScreen)` inside `WanderingTableTheme` (theme lives in `:core:uikit`).
- `:core:domain` — pure Kotlin: entities (`domain/model`), repository interfaces (`domain/repository`, return `Flow<Result<T>>`), `Result` + flow helpers, `AppException` hierarchy (`NetworkException`, `LocalException`).
- `:core:data` — `ResultFlow` (offlineOnly/onlineOnly/offlineFirst, repositories only) and `withErrorHandling` (data sources: maps framework exceptions to `AppException`). Retrofit/Room are not wired yet — add per feature.
- `:core:presentation` — MVI base (`MviViewModel<State, Event, Effect>`, `ObserveAsEvents`), navigation (Nav3 Router/Command/`NavigationHost`, package `core.presentation.navigation` — there is **no** separate `:core:navigation` module), Parcelable UI configs (`InfoScreenConfig`, `SnackbarScreenConfig`, `ButtonConfig`, `Action`) and resources (`TextResource`, `IconResource`) with error→config mappings (`AppException.infoScreenConfig` / `.snackbarConfig`).
- Screens are `@Serializable` subclasses of `ComposableScreen`; navigation goes only through `Router.execute(Forward/Replace/Back/BackTo/NewRoot/ShowSnackbar(config))` from ViewModels. Details — see the `android-navigation` and `android-presentation-mvi` skills.


# Skills 

You can find all instructions about architecture in skills folder.
You have to write all updates using these skills.
The skills will be updated as the application grows.