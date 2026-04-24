package com.scoreeboard.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.scoreeboard.app.data.DraftsRepository
import com.scoreeboard.app.data.GamesRepository
import com.scoreeboard.app.data.PhotoRepository
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
    private val draftsRepository: DraftsRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    // ── Active game state ────────────────────────────────────────────────────

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _draftScores = MutableStateFlow<Map<Int, String>>(emptyMap())
    val draftScores: StateFlow<Map<Int, String>> = _draftScores.asStateFlow()

    // ── Session ids ──────────────────────────────────────────────────────────

    /** Stable id for the current game session (draft / save key). */
    private var currentSessionId: String? = null

    /** Id of the most recently saved [GameRecord]; used by photo functions. */
    private var lastSavedGameId: String? = null

    // ── History state ────────────────────────────────────────────────────────

    private val _history = MutableStateFlow<List<GameRecord>>(emptyList())
    val history: StateFlow<List<GameRecord>> = _history.asStateFlow()

    // ── Drafts state ─────────────────────────────────────────────────────────

    private val _drafts = MutableStateFlow<List<DraftGame>>(emptyList())
    val drafts: StateFlow<List<DraftGame>> = _drafts.asStateFlow()

    // ── Photo state (Summary screen) ──────────────────────────────────────────

    /** URI of the photo attached to the just-finished game, or null. */
    private val _currentPhotoUri = MutableStateFlow<String?>(null)
    val currentPhotoUri: StateFlow<String?> = _currentPhotoUri.asStateFlow()

    init {
        _history.value = gamesRepository.loadAll()
        _drafts.value = draftsRepository.loadAll()
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    fun startGame(title: String, playerNames: List<String>) {
        require(playerNames.size in 2..10) { "Player count must be between 2 and 10." }
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

    fun updateRound(roundNumber: Int, newScores: Map<Int, Int>) {
        val state = _gameState.value
        _gameState.value = state.copy(
            rounds = state.rounds.map { round ->
                if (round.number == roundNumber) round.copy(scores = newScores) else round
            }
        )
    }

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

    /** Transition PLAYING → SUMMARY, persist the completed game, and clean up draft. */
    fun endGame() {
        val state = _gameState.value
        _gameState.value = state.copy(phase = GamePhase.SUMMARY)

        val id = System.currentTimeMillis().toString()
        lastSavedGameId = id
        _currentPhotoUri.value = null

        val record = GameRecord(
            id       = id,
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

    fun abortGame() {
        currentSessionId?.let { draftsRepository.deleteDraft(it) }
        currentSessionId = null
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
        _drafts.value = draftsRepository.loadAll()
    }

    // ── Photo — Summary screen ────────────────────────────────────────────────

    /**
     * Creates a MediaStore entry for the camera to write into.
     * The returned URI must be passed to TakePicture; call [onCameraPhotoTaken]
     * on success or [deletePendingCameraUri] on cancellation.
     */
    fun createCameraUri(): Uri? = photoRepository.createCameraUri(lastSavedGameId ?: "tmp")

    /** Called after a successful camera capture — photo is already in MediaStore. */
    fun onCameraPhotoTaken(cameraUri: Uri) {
        updateCurrentGamePhoto(cameraUri.toString())
    }

    /** Copies a gallery-picked URI into Pictures/ScoreeBoard and attaches it. */
    fun attachPhotoFromGallery(sourceUri: Uri) {
        val id = lastSavedGameId ?: return
        val saved = photoRepository.copyToGallery(sourceUri, id) ?: return
        updateCurrentGamePhoto(saved.toString())
    }

    /** Removes the photo from the current game and deletes it from MediaStore. */
    fun removePhoto() {
        val id = lastSavedGameId ?: return
        _currentPhotoUri.value?.let { photoRepository.deletePhoto(it) }
        _currentPhotoUri.value = null
        updateRecordPhoto(id, null)
    }

    /** Removes a cancelled camera URI that was never confirmed. */
    fun deletePendingCameraUri(uri: Uri) {
        photoRepository.deletePhoto(uri.toString())
    }

    // ── Photo — History / sharing ─────────────────────────────────────────────

    /**
     * Generates a 1080×1080 share image (photo + dark overlay + scores).
     * Returns a FileProvider URI, or null on failure.
     */
    fun createShareImage(record: GameRecord): Uri? =
        try { photoRepository.createShareImage(record) } catch (_: Exception) { null }

    // ── History ───────────────────────────────────────────────────────────────

    fun deleteGame(id: String) {
        // Also clean up the photo from MediaStore
        _history.value.find { it.id == id }?.photoPath?.let { photoRepository.deletePhoto(it) }
        gamesRepository.deleteGame(id)
        _history.value = gamesRepository.loadAll()
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    fun newGame() {
        _gameState.value = GameState()
        _draftScores.value = emptyMap()
        currentSessionId = null
        lastSavedGameId = null
        _currentPhotoUri.value = null
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun resetDraft(players: List<Player>) {
        _draftScores.value = players.associate { it.id to "" }
    }

    fun isDraftValid(): Boolean =
        _draftScores.value.values.all { it.isBlank() || it.toIntOrNull() != null }

    private fun updateCurrentGamePhoto(uriString: String) {
        _currentPhotoUri.value = uriString
        updateRecordPhoto(lastSavedGameId ?: return, uriString)
    }

    private fun updateRecordPhoto(gameId: String, photoUri: String?) {
        val record = gamesRepository.loadAll().find { it.id == gameId } ?: return
        gamesRepository.updateGame(record.copy(photoPath = photoUri))
        _history.value = gamesRepository.loadAll()
    }
}
