package com.scoreeboard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.scoreeboard.app.data.DraftsRepository
import com.scoreeboard.app.data.GamesRepository
import com.scoreeboard.app.model.DraftGame
import com.scoreeboard.app.model.GamePhase
import com.scoreeboard.app.model.GameRecord
import com.scoreeboard.app.model.GameState
import com.scoreeboard.app.model.Player
import com.scoreeboard.app.model.Round
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val gamesRepository: GamesRepository,
    private val draftsRepository: DraftsRepository
) : ViewModel() {

    // ── Active game state ────────────────────────────────────────────────────

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Draft scores being entered for the current round.
     * Values are [String] so blank fields are never coerced to "0".
     * Key = Player.id.
     */
    private val _draftScores = MutableStateFlow<Map<Int, String>>(emptyMap())
    val draftScores: StateFlow<Map<Int, String>> = _draftScores.asStateFlow()

    // ── Stable session id ────────────────────────────────────────────────────
    // Assigned once per game session; used to upsert and delete the draft.

    private var currentSessionId: String? = null

    // ── History state ────────────────────────────────────────────────────────

    private val _history = MutableStateFlow<List<GameRecord>>(emptyList())
    val history: StateFlow<List<GameRecord>> = _history.asStateFlow()

    // ── Drafts state ─────────────────────────────────────────────────────────

    private val _drafts = MutableStateFlow<List<DraftGame>>(emptyList())
    val drafts: StateFlow<List<DraftGame>> = _drafts.asStateFlow()

    init {
        _history.value = gamesRepository.loadAll()
        _drafts.value = draftsRepository.loadAll()
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    fun startGame(title: String, playerNames: List<String>) {
        require(playerNames.size in 2..6) { "Player count must be between 2 and 6." }
        val players = playerNames.mapIndexed { idx, name ->
            Player(id = idx, name = name.trim().ifBlank { "Player ${idx + 1}" })
        }
        currentSessionId = System.currentTimeMillis().toString()
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

    /** Replace the scores of an already-submitted round. */
    fun updateRound(roundNumber: Int, newScores: Map<Int, Int>) {
        val state = _gameState.value
        _gameState.value = state.copy(
            rounds = state.rounds.map { round ->
                if (round.number == roundNumber) round.copy(scores = newScores) else round
            }
        )
    }

    /** Save the current game as a draft so it can be resumed later. */
    fun saveDraft() {
        val id = currentSessionId ?: return
        val state = _gameState.value
        val draft = DraftGame(
            id = id,
            title = state.title,
            savedAt = System.currentTimeMillis(),
            players = state.players,
            rounds = state.rounds
        )
        draftsRepository.saveDraft(draft)
        _drafts.value = draftsRepository.loadAll()
    }

    /** Restore a saved draft as the active game session. */
    fun resumeGame(draft: DraftGame) {
        currentSessionId = draft.id
        _gameState.value = GameState(
            title = draft.title,
            players = draft.players,
            rounds = draft.rounds,
            phase = GamePhase.PLAYING
        )
        resetDraft(draft.players)
    }

    /** Transition PLAYING → SUMMARY, persist the completed game, and remove its draft. */
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
        gamesRepository.saveGame(record)
        _history.value = gamesRepository.loadAll()

        currentSessionId?.let { draftsRepository.deleteDraft(it) }
        _drafts.value = draftsRepository.loadAll()
    }

    /** Abandon the current game without saving, and remove its draft. */
    fun abortGame() {
        currentSessionId?.let { draftsRepository.deleteDraft(it) }
        currentSessionId = null
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
        _drafts.value = draftsRepository.loadAll()
    }

    // ── History ──────────────────────────────────────────────────────────────

    fun deleteGame(id: String) {
        gamesRepository.deleteGame(id)
        _history.value = gamesRepository.loadAll()
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    fun newGame() {
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
        currentSessionId = null
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun resetDraft(players: List<Player>) {
        _draftScores.value = players.associate { it.id to "" }
    }

    fun isDraftValid(): Boolean =
        _draftScores.value.values.all { it.isBlank() || it.toIntOrNull() != null }
}
