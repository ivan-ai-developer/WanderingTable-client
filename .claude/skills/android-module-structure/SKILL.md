---
name: android-module-structure
description: Module layout, dependency rules, and Gradle convention plugins for Android and Kotlin Multiplatform (KMP) projects. Use this skill whenever setting up a new Android project, deciding where a new module should live, asking "how should I structure this", creating a new feature module, adding a core submodule, configuring Gradle convention plugins, working with version catalogs, or making any decision about project-level architecture. Trigger on phrases like "set up the project", "add a module", "create a feature", "how should I structure", "project structure", "convention plugin", "build-logic", or "where does X live".
---

# Android Module Structure

## Core Philosophy

- **Feature-layered modularization**: split by feature first, then by layer within each feature.
- **Clean Architecture layers**: `presentation` → `domain` ← `data`. Domain is innermost and depends on nothing.
- **Code lives in a feature module unless it is needed by more than one feature** — then it moves to the appropriate `core` submodule.
- Features **never depend on each other**. Cross-feature shared data belongs in `core:domain` (domain models) or `core:presentation` (shared composables/UI logic), not in the owning feature.

---

## Module Layout
 
```
:app
:build-logic                    ← Gradle convention plugins
:core:domain                    ← Entities, repository interfaces, error types, Result
:core:presentation              ← MVI base (MviViewModel), navigation (Router/Command/NavigationHost), configs, TextResource/IconResource, ObserveAsEvents
:core:uikit                     ← Reusable Compose components, colors, theme, typography
:data:<name>                    ← Repo implementations, DTOs, DBOs, mappers, Room DAOs, Retrofit api
:feature:<name>                 ← ViewModel, screen composables, state, actions, events
```
 
For standalone, self-contained concerns that involve meaningful complexity (multiple classes, configuration, or a non-trivial API surface), create a dedicated module under `:core` (e.g., `:core:location`, `:core:analytics`). Do not create a separate module for a single class or a trivial utility — that belongs in an existing `core` module instead.
 
---
 
## Dependency Rules
 
| Layer | May depend on |
|---|---|
| `feature:<name>` | `core:domain`, `core:presentation`, `core:uikit` |
| `data:<name>` | `core:domain`, `core:data` |
| `:app` | everything (wires all modules) |

**Every** layer and module may access `core:domain`.
 
---

## Convention Plugins (`:build-logic`)
 
Define a convention plugin for every non-trivial Gradle config:
 
| Plugin | Purpose                                                            |
|---|--------------------------------------------------------------------|
| `android-application` | App module config (applicationId, versionCode, etc.)               |
| `android-library` | Base Android library config                                        |
| `android-feature` | Android library + Compose + Dagger Hilt + shared feature deps bundled |
| `domain-module` | Pure Kotlin/KMP module, no Android deps                            |
| `compose` | Compose compiler + BOM                                             |
| `dagger-hilt` | Dagger-Hilt dependency block                                       |
| `Retrofit` | Retrofit client + serialization                                    |
| `room` | Room + KSP config                                                  |
| `kotlinx-serialization` | KotlinX Serialization plugin + dep                                 |
 
Use **version catalogs** (`libs.versions.toml`) for all dependency and version management. No hardcoded versions in build files.
 
---
 
## Key Libraries
 
| Concern | Library                                                           |
|---|-------------------------------------------------------------------|
| DI | Dagger-hilt                                                       |
| Networking | Retrofit Client                                                   |
| Local DB | Room                                                              |
| Preferences | DataStore                                                         |
| Navigation | Jetpack Navigation 3 (Router/Screen/Command — see android-navigation skill) |
| Serialization | KotlinX Serialization (retrofit + Nav routes)                     |
| Image loading | Coil                                                              |
| Logging | Kermit                                                            |
| Async | Coroutines + Flow                                                 |
| Background tasks | WorkManager                                                       |
| Secrets | `local.properties` + `BuildConfig` (Android); `BuildKonfig` (KMP) |
| Testing | JUnit5, Turbine, AssertK, `kotlinx-coroutines-test`               |
| UI testing | `ComposeTestRule`                                                 |
 
---
 
## Checklist: Adding a New Feature Module
 
- [ ] Create `:feature:<name>`, `:data:<name>` (if need) modules
- [ ] Apply appropriate convention plugins to each module (`domain-module`, `android-library`/`android-feature`)
- [ ] Verify no cross-feature dependencies are introduced