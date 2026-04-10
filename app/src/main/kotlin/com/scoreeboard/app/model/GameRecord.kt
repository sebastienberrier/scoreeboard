package com.scoreeboard.app.model

import kotlinx.serialization.Serializable

/**
 * Immutable snapshot of a completed game, stored in local history.
 * Reuses [Player] and [Round] which are already [Serializable].
 */
@Serializable
data class GameRecord(
    /** Unique identifier — epoch millis as string, sufficient for a local store. */
    val id: String,
    val title: String,
    /** Unix epoch in milliseconds — used for display and sorting. */
    val playedAt: Long,
    val players: List<Player>,
    val rounds: List<Round>
) {
    /** Cumulative total for a given player. */
    fun totalFor(playerId: Int): Int =
        rounds.sumOf { it.scores[playerId] ?: 0 }

    /** Players sorted by descending total (stable sort for ties). */
    fun ranking(): List<Pair<Player, Int>> =
        players
            .map { p -> p to totalFor(p.id) }
            .sortedByDescending { it.second }
}
