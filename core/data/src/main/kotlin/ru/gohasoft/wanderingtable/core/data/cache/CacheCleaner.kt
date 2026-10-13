package ru.gohasoft.wanderingtable.core.data.cache

/**
 * Drops what a data module stored for the signed-in account.
 *
 * Logging out clears every registered cleaner, so the next account on the device cannot open on
 * the previous one's data — and no screen has to remember to ask for it.
 *
 * A seam rather than a repository call: the stores live in feature data modules, and the
 * implementation of `AuthRepository` cannot depend on them. Each module binds its local data
 * source `@IntoSet`, the same way `:data:auth` fulfils `:core:network`'s token hooks; a build with
 * none of them gets an empty set.
 *
 * It lives here and not in `:core:domain` because nothing above the data layer calls it: a
 * ViewModel never needs to ask for this, and one that did would be a sign the wipe had been left
 * to the caller again.
 *
 * **`:data:auth`'s own store is deliberately not one of these.** It holds the session rather than
 * a copy of anything, `logOut` has to read the refresh token out of it before the round trip and
 * clear it after, and failing to clear it is a real failure — the user would still be signed in —
 * whereas a cleaner that throws is swallowed. Implementing this there would make the repository
 * clear itself through its own set, in an order it no longer controls.
 */
interface CacheCleaner {

    suspend fun clear()
}
