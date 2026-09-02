# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

**Wandering Table** — a tabletop board game club app. The core layer, the auth flow and the main feature are implemented. After login the app opens `MainShellScreen` (`:feature:main`), which hosts the Home / Games / Profile tabs plus the Create sheet; the temporary Welcome screen is gone. Nine screens follow the design: Home, Games, Game Detail, Create Request, News Detail, Notifications, Notification Settings, Profile and the Create sheet. Two more have no mockup and exist because the Create sheet needs destinations: Create News (`POST /notes`) and Create Game (`POST /games` — without it a fresh club has an empty catalogue and no request can be posted at all).

Not built: tournaments, championships and leagues. The server supports them in full (`/events/tournaments`), but the design has no screens for them, so `:feature:main` filters the schedule down to `REGULAR_GAME`.

The remaining feature set can be inferred from the design mockups in `specs/` (`Wandering Table App Design.html` and `Wandering Table UI Kit.html`, bundled Figma-style exports — large single-file HTML, not meant to be edited by hand). They describe a club app for finding opponents and hosting board game sessions: sign up / log in, browse a game library (e.g. Chess, Azul, Wingspan, Settlers of Catan, Terraforming Mars), post/browse "Find an Opponent" requests with skill level and location, host/join games, club news and announcements, notifications, and a user profile with stats (wins, skill level).

When implementing new features, treat the design HTML files as the source of truth for screen names, copy, and flows, but do not attempt to parse or programmatically extract from them beyond visual/text reference.

The backend contract lives outside this repo, at `C:\Users\gohms\Desktop\wanderingtable\ANDROID_CLIENT_API.md` (reachable through the shortcut in `specs/`). Read it before adding any endpoint — several design elements have no server field behind them; the ones that matter are listed under "Design vs. API" below.

## Memory Tools (MCP)

- Always prioritize `codebase-memory` tools (`trace_call_path`, `query_graph`, `get_architecture`) over global text search (`grep`).
- Use the knowledge graph to understand structural relations, call hierarchies, and architecture before modifying files. This saves context tokens.
- After major changes (changes caused by the /plan-mode command) you can update Claude.md.

## Build system

- Gradle (Kotlin DSL), Android Gradle Plugin 9.3.1, Kotlin 2.3.21, convention plugins in `build-logic/` (`wanderingtable.android.application`, `.android.library`, `.compose`, `.domain.module`, `.hilt`).
- Modules: `:app`, `:core:domain` (pure Kotlin), `:core:data` (pure Kotlin), `:core:network` (OkHttp/Retrofit + token pipeline), `:core:presentation` (MVI + navigation + configs), `:core:uikit`, `:feature:auth`, `:feature:main`, `:data:auth`, `:data:main`.
- `compileSdk`/`targetSdk` 36, `minSdk` 28.
- UI toolkit: Jetpack Compose (Material 3), via the `androidx.compose.bom` (2026.06.01) and the `org.jetbrains.kotlin.plugin.compose` plugin — no XML layouts.
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
- `:core:data` — pure Kotlin: `ResultFlow` (offlineOnly/onlineOnly/offlineFirst, repositories only) and `withErrorHandling` (data sources: maps framework exceptions to `AppException` by HTTP status). No Android/OkHttp here.
- `:core:network` — the shared HTTP stack: `Json`, two OkHttp/Retrofit pairs behind the `@PlainClient` / `@AuthenticatedClient` qualifiers, `AuthInterceptor` (Bearer header) and `TokenAuthenticator` (serialized, single-flight refresh on 401). It never depends on a data module: `:data:auth` fulfils its `TokenProvider` / `TokenRefresher` interfaces via Hilt. New feature APIs take `@AuthenticatedClient Retrofit`; only public endpoints use `@PlainClient`. `BASE_URL` comes from the `wt.baseUrl` Gradle property (default `http://10.0.2.2:8050/`, the emulator's host alias) — a new host also needs an entry in `app/src/main/res/xml/network_security_config.xml` while the dev backend is cleartext.
- `:data:auth` — the `AuthRepository` implementation. Tokens live in a Preferences DataStore, AES/GCM-encrypted with an Android Keystore key (`KeystoreTokenCipher`); the user profile is cached alongside them in the clear. Token expiry is handled reactively (401 → refresh), never by inspecting a JWT. `GET /users/me` is the only source of the signed-in user's id/name/roles. Room is not wired anywhere yet — add per feature.
- `:core:presentation` — MVI base (`MviViewModel<State, Event, Effect>`; screens are wired with the `MviContent` helper in `ObserveEffects.kt`, which collects effects and hands the composable `state` + an `EventHandler` receiver), navigation (Nav3 Router/Command/`NavigationHost`, package `core.presentation.navigation` — there is **no** separate `:core:navigation` module), Parcelable UI configs (`InfoScreenConfig`, `SnackbarScreenConfig`, `ButtonConfig`, `Action`) and resources (`TextResource`, `IconResource`) with error→config mappings (`AppException.infoScreenConfig` / `.snackbarConfig`).
- `:data:main` — the main feature's data layer: `GameRepository`, `GameEventRepository`, `NewsRepository`, `UserRepository`, `DeviceRepository` over `/games`, `/events`, `/notes`, `/users/me` and `/users/me/devices`, plus two DataStore-backed ones (`NotificationRepository`, `NotificationSettingsRepository`) that have no server side at all. Its DataStore is qualified `@MainPreferences` — `:data:auth` already provides an unqualified `DataStore<Preferences>`. Mappers drop rows carrying an enum value or timestamp this build cannot model, so one unknown row never blanks a list; the single-object variants raise `SerializationException` instead.
- Screens are `@Serializable` subclasses of `ComposableScreen`; navigation goes only through `Router.execute(Forward/Replace/Back/BackTo/NewRoot/ShowSnackbar(config))` from ViewModels. Details — see the `android-navigation` and `android-presentation-mvi` skills.
- `:feature:main` keeps a single back-stack entry while the user is inside the app: `MainShellScreen` owns the bottom bar, and Home / Games / Profile are plain composables inside it (each with its own `hiltViewModel`, scoped to that one `NavEntry`). Switching tabs is state, not navigation, so system back leaves the app rather than unwinding tabs. The bar's fourth slot, Create, opens a sheet instead of switching tabs. Detail screens (Game Detail, News Detail, Create Request, Create News, Notifications, Notification Settings) are real `ComposableScreen`s pushed on top.
- Club Administration (`:feature:main/clubadmin`) is the Profile row only a `CLUB_MANAGER` sees. It manages the manager's own roles — the supported way to grant yourself `GAME_CREATOR` so the catalogue can be filled — and grants roles to another member found by email. Both halves send the **full** role set, because `PATCH /users/{id}/roles` replaces rather than adds; that is why `GET /users?email=` returns the target's current roles. `CLUB_MANAGER` cannot be revoked from yourself (the server answers 409), so that switch is inert on your own account.
- The Create sheet is role-gated: each option is present only when the account holds the exact role its endpoint demands — `NEWS_CREATOR` for Post Club News, `GAME_CREATOR` for Add a Game — so no option in it can end in a 403. `CLUB_MANAGER` is deliberately **not** treated as a superset. There is no role hierarchy: the server maps one authority per stored role (`JwtAuthFilter` → `ROLE_${it.name}`), has no `RoleHierarchy` bean, and guards creation with `@PreAuthorize("hasRole('GAME_CREATOR')")` / `hasRole('NEWS_CREATOR')`. Where a manager does get extra power it is written out by hand (`NewsNoteService.delete`, `EventService`). So an account holding only `CLUB_MANAGER` gets a 403 from `POST /games` and `POST /notes` — it grants itself the specific role via `PATCH /users/{id}/roles` instead. Verified against the server sources; re-check them before loosening this. A manager who needs one of those roles grants it to themselves in Club Administration.
- Push: `PushTokenProvider` (`:core:domain/push`) is the seam; `:app` implements it with Firebase (`FirebasePushTokenProvider`) and hosts `WanderingTableMessagingService`, which writes incoming pushes into the local feed and raises a system notification when `NotificationSettings` allows it. `MainShellViewModel` binds the token on launch; Profile unbinds it on logout. **`app/google-services.json` is optional** — the `com.google.gms.google-services` plugin is applied only if the file exists, and every push call is best effort, so the app builds and runs without it (just without push).

## Design vs. API

Four things the mockups show have no server field behind them. Do not invent endpoints for them; the current handling is deliberate:

- **Skill level and table/location on a play.** `POST /events/regular-games` accepts only `gameId`, `title`, `description`, `startsAt`, `durationMinutes` and the participant bounds. Create Request still renders both controls (validated locally) but never sends them, and every card in the schedule reads "Any level". See `SkillLevelUi`.
- **Another member's name.** There is no user-lookup endpoint — only `GET /users/me` and `/users/{id}/stats` (which has no name). Cards say "Hosted by you" or "Hosted by a club member", and avatars for other members are `?`.
- **News category and cover image.** `/notes` stores id, title, content, createdAt and ownerId. The category badge is dropped, the cover slot renders `CoverPlaceholder`, and the byline is "by you" or "by the club".
- **A single news post.** There is no `GET /notes/{id}`; `NewsRepository.getNewsItem` reads the feed and picks the match, raising `NetworkException.NotFound` when it is gone.

Also: the design's "Member since" has no field (accounts carry no creation date) and the profile's "Level" is derived from total wins — the server ranks players only inside a league.


# Skills 

You can find all instructions about architecture in skills folder.
You have to write all updates using these skills.
The skills will be updated as the application grows.