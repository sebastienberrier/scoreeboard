package com.scoreeboard.app.model

import kotlinx.serialization.Serializable

/**
 * Immutable identity of a player.
 * [id] is the 0-based index assigned at setup time.
 */
@Serializable
data class Player(
    val id: Int,
    val name: String
)

/**
 * Scores submitted for a single round.
 * [scores] maps each Player.id to the points earned in that round.
 * A missing entry defaults to 0.
 * Note: Int keys are serialized as strings in JSON, which round-trips correctly.
 */
@Serializable
data class Round(
    val number: Int,           // 1-based display number
    val scores: Map<Int, Int>  // key = Player.id
)

/**
 * Lifecycle phase of the current game session.
 */
enum class GamePhase { SETUP, PLAYING, SUMMARY }

/**
 * Immutable snapshot of the entire game session.
 * Every mutation produces a new copy via [copy]; nothing is ever mutated in place.
 */
data class GameState(
    val title: String = "",
    val players: List<Player> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val phase: GamePhase = GamePhase.SETUP
) {
    /** Cumulative total score for a given player. */
    fun totalFor(playerId: Int): Int =
        rounds.sumOf { it.scores[playerId] ?: 0 }

    /**
     * Players sorted by descending total score.
     * Ties preserve the original player order (stable sort).
     */
    fun ranking(): List<Pair<Player, Int>> =
        players
            .map { p -> p to totalFor(p.id) }
            .sortedByDescending { it.second }
}
