package ru.gohasoft.wanderingtable.data.main.local.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * The signed-in user's profile, one row at a time ([SINGLETON_ID]).
 *
 * Statistics are recomputed server-side on every read, so these numbers are a snapshot for the
 * offline case, not a source of truth — a fetch always overwrites them. The row is wiped on
 * logout, otherwise the next account on the device would briefly see this one's stats.
 */
@Entity(tableName = "profile")
internal data class CachedProfileDbo(
    @PrimaryKey val id: String = SINGLETON_ID,
    val userId: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val roles: List<String>,
    val gamesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val favoriteGames: List<FavoriteGameDbo>,
) {
    companion object {
        const val SINGLETON_ID = "me"
    }
}

@Serializable
internal data class FavoriteGameDbo(
    val gameId: String,
    val name: String,
    val playedCount: Int,
)
