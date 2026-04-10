package com.scoreeboard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.scoreeboard.app.data.GamesRepository
import com.scoreeboard.app.model.GamePhase
import com.scoreeboard.app.model.GameRecord
import com.scoreeboard.app.model.GameState
import com.scoreeboard.app.model.Player
import com.scoreeboard.app.model.Round
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(private val repository: GamesRepository) : ViewModel() {

    // ── Active game state ────────────────────────────────────────────────────

    /** Single source of truth for the current game session. */
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Draft scores being entered for the current round.
     * Values are [String] so blank fields are never coerced to "0".
     * Key = Player.id.
     */
    private val _draftScores = MutableStateFlow<Map<Int, String>>(emptyMap())
    val draftScores: StateFlow<Map<Int, String>> = _draftScores.asStateFlow()

    // ── History state ────────────────────────────────────────────────────────

    private val _history = MutableStateFlow<List<GameRecord>>(emptyList())
    val history: StateFlow<List<GameRecord>> = _history.asStateFlow()

    init {
        _history.value = repository.loadAll()
    }

    // ── Setup ────────────────────────────────────────────────────────────────

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

    fun updateDraftScore(playerId: Int, raw: String) {
        _draftScores.value = _draftScores.value + (playerId to raw)
    }

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

    /** Transition PLAYING → SUMMARY and persist the completed game. */
    fun endGame() {
        val state = _gameState.value
        _gameState.value = state.copy(phase = GamePhase.SUMMARY)

        val record = GameRecord(
            id       = System.currentTimeMillis().toString(),
            title    = state.title,
            playedAt = System.currentTimeMillis(),
            players  = state.players,
            rounds   = state.rounds
        )
        repository.saveGame(record)
        _history.value = repository.loadAll()
    }

    /** Abandon the current game and return to SETUP without saving. */
    fun abortGame() {
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    fun newGame() {
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun resetDraft(players: List<Player>) {
        _draftScores.value = players.associate { it.id to "" }
    }

    fun isDraftValid(): Boolean =
        _draftScores.value.values.all { it.isBlank() || it.toIntOrNull() != null }
}
