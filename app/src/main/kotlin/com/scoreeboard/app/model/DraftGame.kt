package com.scoreeboard.app.model

import kotlinx.serialization.Serializable

/**
 * A game session saved mid-play so it can be resumed later.
 * The [id] is stable for the lifetime of the session; it is used
 * to upsert (save again after more rounds) and to delete the draft
 * when the game is eventually ended or aborted.
 */
@Serializable
data class DraftGame(
    val id: String,
    val title: String,
    val savedAt: Long,
    val players: List<Player>,
    val rounds: List<Round>
)
