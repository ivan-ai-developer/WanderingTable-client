# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This is a fresh Android Studio project scaffold (default "Empty Activity" Compose template) for **Wandering Table**, a tabletop board game club app. Only the generated boilerplate exists so far (`MainActivity.kt`, theme files, default tests) — no app-specific screens or logic have been implemented yet.

The intended feature set can be inferred from the design mockups in `design/` (`Tabletop Club App.html` and `Wandering Table UI Kit.html`, bundled Figma-style exports — large single-file HTML, not meant to be edited by hand). They describe a club app for finding opponents and hosting board game sessions: sign up / log in, browse a game library (e.g. Chess, Azul, Wingspan, Settlers of Catan, Terraforming Mars), post/browse "Find an Opponent" requests with skill level and location, host/join games, club news and announcements, notifications, and a user profile with stats (wins, skill level).

When implementing new features, treat the design HTML files as the source of truth for screen names, copy, and flows, but do not attempt to parse or programmatically extract from them beyond visual/text reference.

## Build system

- Gradle (Kotlin DSL), Android Gradle Plugin 9.2.1, Kotlin 2.2.10.
- Single module: `:app`, package/namespace `ru.gohasoft.wanderingtable`.
- `compileSdk`/`targetSdk` 36, `minSdk` 28.
- UI toolkit: Jetpack Compose (Material 3), via the `androidx.compose.bom` (2026.02.01) and the `org.jetbrains.kotlin.plugin.compose` plugin — no XML layouts.
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog); add new dependencies there rather than hardcoding versions in `app/build.gradle.kts`.

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

- Entry point is `MainActivity` (`app/src/main/java/ru/gohasoft/wanderingtable/MainActivity.kt`), a single `ComponentActivity` using `setContent { }` with edge-to-edge enabled — standard for a Compose-only app with no Activity/Fragment navigation graph yet.
- Theming lives in `app/src/main/java/ru/gohasoft/wanderingtable/ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`), following the standard Compose Material 3 template structure: `WanderingTableTheme` picks between dynamic color (Android 12+), and static light/dark `ColorScheme`s as a fallback.
- No dependency injection framework, networking, persistence, or navigation library is wired up yet — these will need to be chosen/added as the app grows past the scaffold stage.
