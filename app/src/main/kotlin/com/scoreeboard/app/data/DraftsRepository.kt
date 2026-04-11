package com.scoreeboard.app.data

import android.content.Context
import com.scoreeboard.app.model.DraftGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists in-progress [DraftGame]s as a JSON file in the app's internal storage.
 *
 * Storage location: [Context.filesDir]/draft_games.json
 * Saving the same id twice is an upsert — the existing entry is replaced.
 */
class DraftsRepository(context: Context) {

    private val file = File(context.filesDir, "draft_games.json")

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    /** Save or update a draft (upsert by [DraftGame.id]). */
    fun saveDraft(draft: DraftGame) {
        val drafts = loadAll().filterNot { it.id == draft.id }.toMutableList()
        drafts.add(0, draft)   // newest first
        file.writeText(json.encodeToString(drafts))
    }

    /** Remove the draft with the given [id]. No-op if not found. */
    fun deleteDraft(id: String) {
        val drafts = loadAll().filterNot { it.id == id }
        file.writeText(json.encodeToString(drafts))
    }

    /** Return all saved drafts (newest first), or an empty list if none. */
    fun loadAll(): List<DraftGame> {
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<DraftGame>>(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }
}
