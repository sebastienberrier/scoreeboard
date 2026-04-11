package com.scoreeboard.app.data

import android.content.Context
import com.scoreeboard.app.model.GameRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists completed [GameRecord]s as a JSON file in the app's internal storage.
 *
 * Storage location: [Context.filesDir]/games_history.json
 * Format: a JSON array of [GameRecord], newest entry first.
 * Thread-safety: all operations are synchronous; call from a coroutine if needed.
 */
class GamesRepository(context: Context) {

    private val file = File(context.filesDir, "games_history.json")

    private val json = Json {
        prettyPrint     = false
        ignoreUnknownKeys = true   // forward-compatible with future fields
    }

    /** Persist a newly completed game at the front of the list. */
    fun saveGame(record: GameRecord) {
        val games = loadAll().toMutableList()
        games.add(0, record)       // newest first
        file.writeText(json.encodeToString(games))
    }

    /** Remove the game with the given [id]. No-op if not found. */
    fun deleteGame(id: String) {
        val games = loadAll().filterNot { it.id == id }
        file.writeText(json.encodeToString(games))
    }

    /** Return all stored games (newest first), or an empty list if none yet. */
    fun loadAll(): List<GameRecord> {
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<GameRecord>>(file.readText())
        } catch (e: Exception) {
            emptyList()            // corrupted file → silently return empty
        }
    }
}
