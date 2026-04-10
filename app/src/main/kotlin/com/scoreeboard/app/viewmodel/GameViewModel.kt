package com.scoreeboard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.scoreeboard.app.model.GamePhase
import com.scoreeboard.app.model.GameState
import com.scoreeboard.app.model.Player
import com.scoreeboard.app.model.Round
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {

    // ── Exposed state ────────────────────────────────────────────────────────

    /** Single source of truth for the game session. */
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Draft scores being entered for the current round.
     * Values are kept as [String] so that blank fields are not coerced to "0"
     * and validation can be handled independently of parsing.
     * Key = Player.id.
     */
    private val _draftScores = MutableStateFlow<Map<Int, String>>(emptyMap())
    val draftScores: StateFlow<Map<Int, String>> = _draftScores.asStateFlow()

    // ── Setup ────────────────────────────────────────────────────────────────

    /**
     * Transition from SETUP → PLAYING.
     * Blank names are replaced with "Player N".
     */
    fun startGame(title: String, playerNames: List<String>) {
        require(playerNames.size in 2..6) { "Player count must be between 2 and 6." }
        val players = playerNames.mapIndexed { idx, name ->
            Player(id = idx, name = name.trim().ifBlank { "Player ${idx + 1}" })
        }
        _gameState.value = GameState(
            title = title.trim(),
            players = players,
            rounds = emptyList(),
            phase = GamePhase.PLAYING
        )
        resetDraft(players)
    }

    // ── In-game ──────────────────────────────────────────────────────────────

    /** Update one player's draft score while they are typing. */
    fun updateDraftScore(playerId: Int, raw: String) {
        _draftScores.value = _draftScores.value + (playerId to raw)
    }

    /**
     * Commit the current draft as a new [Round].
     * Blank or invalid entries are treated as 0 (graceful degradation).
     */
    fun submitRound() {
        val state = _gameState.value
        val roundNumber = state.rounds.size + 1
        val scores: Map<Int, Int> = state.players.associate { p ->
            p.id to (_draftScores.value[p.id]?.toIntOrNull() ?: 0)
        }
        val newRound = Round(number = roundNumber, scores = scores)
        _gameState.value = state.copy(rounds = state.rounds + newRound)
        resetDraft(state.players)
    }

    /** Transition from PLAYING → SUMMARY. */
    fun endGame() {
        _gameState.value = _gameState.value.copy(phase = GamePhase.SUMMARY)
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    /** Reset everything back to the initial SETUP state. */
    fun newGame() {
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun resetDraft(players: List<Player>) {
        _draftScores.value = players.associate { it.id to "" }
    }

    /**
     * Returns true if every draft field is either blank or a valid integer.
     * Used to enable/disable the "Submit Round" button.
     */
    fun isDraftValid(): Boolean =
        _draftScores.value.values.all { it.isBlank() || it.toIntOrNull() != null }
}
