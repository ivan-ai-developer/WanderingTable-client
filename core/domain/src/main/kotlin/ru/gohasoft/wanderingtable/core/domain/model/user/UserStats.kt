package ru.gohasoft.wanderingtable.core.domain.model.user

/** Play statistics, recomputed by the server on every read — do not cache it for long. */
data class UserStats(
    val userId: String,
    val gamesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    /** Top games by number of plays, most played first. */
    val favoriteGames: List<FavoriteGame>,
)
