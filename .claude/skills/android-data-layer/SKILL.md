---
name: android-data-layer
description: |
  Data layer patterns for Android - data sources, repositories, DTOs, mappers, Room entities (DBO - Database Object), Retrofit HttpClient, safe call helpers, token storage, and offline-first. Use this skill whenever writing or reviewing a data source or repository, creating DTOs or Room entities, writing mappers, setting up the Retrofit HttpClient, handling network errors, or implementing token refresh. Trigger on phrases like "create a repository", "create a data source", "add a DAO", "Retrofit client", "write a mapper", "DTO", "Room entity", "network call", "token storage", or "offline-first".
---
 
# Android
 
## Error Handling

This skill uses `withErrorHandle<T>`, and the extension helpers defined in the **android-error-handling** skill. Refer to that skill for the full `withErrorHandle` wrapper.


## Data Result

Use Result data class from ru.gohasoft.wanderingtable.core.data package to deliver results of repository work. You have to use ResultFlow data wrap helper in ru.gohasoft.wanderingtable.core.data.repository package (use only in repositories)

---

## Data Source vs Repository

- **Data source** — accesses a single data source (local DB, remote API, file system). Most classes in the data layer are data sources.
- **Repository** — combines multiple data sources (e.g., a remote API + a local DB for offline-first). Only use the term "repository" when the class genuinely coordinates multiple sources.

```kotlin
// Single source → data source
internal class LocalDataSource @Inject constructor(...) {
    fun getNotes(): Flow<List<NoteDBO>> { ... }
    suspend fun insertNote(note: Note) { ... }
}

internal class RemoteDataSource @Inject constructor(...) {
    suspend fun fetchNotes(): List<NoteDTO> { ... }
}

// Multiple sources → repository
interface NoteRepository {
    fun getNotes(): Flow<Result<List<Note>>>
    fun sync(): Flow<Result<Unit>>
}
```

## Domain Layer Contracts

- Pure Kotlin — no Android/framework imports.
- Contains: domain models, repository **interfaces**, error types.
- **Every data source or repository used by a ViewModel must have an interface in `domain`** — enforces that `presentation` never depends on `data`, and enables testing.
 
---
 
## DTOs and Domain Models
 
- Always separate: DTOs/DBOs (data layer) ↔ Domain Models (domain layer).
- Domain models never go directly into Room entities or Retrofit request/response bodies.
- Mappers are simple extension functions living in the data layer alongside the DTO:
 
```kotlin
fun NoteDto.toNote(): Note = Note(id = id, title = title, ...)
fun Note.toNoteDto(): NoteDto = NoteDto(id = id, title = title, ...)
fun NoteDbo.toNote(): Note = ...
fun Note.toNoteDbo(): NoteDbo = ...
```
 
---
 
## Implementations

Name implementations for what makes them unique — never suffix with `Impl`.

### Data source (single source)

```kotlin
internal class LocalDataSource @Inject constructor(private val dao: NoteDao) {
    suspend fun getNotes(): Flow<List<NoteDbo>> {
        return dao.getNotes()
    }
}
```

### Repository (multiple sources)

```kotlin
internal class OfflineFirstNoteRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : NoteRepository {
    override suspend fun getNotes(forceRefresh: Boolean): Flow<Result<List<Note>>> {
        return ResultFlow.offlineFirst(
            query = { localDataSource.getNotes().map { it.map(NoteDbo::toNote) } },
            fetch = { remoteDataSource.fetchNotes() },
            saveFetchResult = { new, old -> ... },
            shouldFetch = { forceRefresh }
        )
    }
}
```

Use names like `OfflineFirstNoteRepository`. The name should tell you what the class wraps or how it behaves.
 
---
 
## Retrofit — OkHttpClient Factory (`core:data`)
 
Configure the client once. Accept the engine externally so tests can swap in a mock engine:
 
Inject `OkHttpClient` via Dagger Hilt.
 
---
 
## Token Storage
 
Store tokens in DataStore (in `:data:auth` module). The Retrofit `Auth` plugin reads/writes tokens and handles 401 refresh automatically.
 
---
 
## Room Migrations
 
Prefer `@Database(autoMigrations = [AutoMigration(from = 1, to = 2)])`. Use manual `Migration` objects when the schema change is too complex for auto-migration.
 
---
 
## Offline-First (when applicable)
 
Follow **Room as single source of truth**: fetch from network → persist to Room → expose DB `Flow` to the ViewModel. The ViewModel never observes network responses directly.
 
This pattern is optional — apply it when the project requires offline support.
 
---
 
## Naming Conventions
 
| Thing | Convention                               | Example                                        |
|---|------------------------------------------|------------------------------------------------|
| Data source | `<Local/Remote>DataSource`               | `LocalDataSource`, `RemoteDataSource`  |
| Repository interface | `<Entity>Repository` (multi-source only) | `NoteRepository`                               |
| Repository impl | describe what makes it unique            | `OfflineFirstNoteRepository`                   |
| DTO | `<Model>Dto`                             | `NoteDto`                                      |
| Room entity | `<Model>Dbo`                             | `NoteDbo`                                      |
| Mapper | extension fun on source type             | `fun NoteDto.toNote()`                         |
 
---
 
## Checklist: Adding a New Data Source or Repository

- [ ] Define domain model(s) in `core:domain`
- [ ] Define repository interface in `core:domain`
- [ ] Define feature-specific error type(s) in `core:domain` (implement `AppException`) — see **android-error-handling** skill
- [ ] Define DTOs and Room DBOs in `data:<name>`
- [ ] Write mappers as extension functions in `data:<name>`
- [ ] Implement data source (single source) or repository (multi-source) in `data:<name>`, named for what makes it unique
 