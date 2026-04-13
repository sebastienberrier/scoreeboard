package com.scoreeboard.app.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.scoreeboard.app.model.GameRecord
import java.io.File
import java.io.FileOutputStream

/**
 * Handles all photo I/O for ScoreeBoard:
 *  - Persisting photos to MediaStore (Pictures/ScoreeBoard/)
 *  - Deleting photos from MediaStore
 *  - Generating the shareable score-overlay image saved to the app cache
 */
class PhotoRepository(private val context: Context) {

    companion object {
        private const val GALLERY_FOLDER = "ScoreeBoard"
        private const val SHARE_IMAGE_DIR = "shared_images"
        private const val FILE_PROVIDER_AUTHORITY = "com.scoreeboard.app.fileprovider"
    }

    // ── Camera ───────────────────────────────────────────────────────────────

    /**
     * Creates a MediaStore entry for the camera to write into.
     * Pass the returned URI to [ActivityResultContracts.TakePicture].
     * If the capture is cancelled, call [deletePhoto] with this URI.
     */
    fun createCameraUri(gameId: String): Uri? {
        val fileName = "scoreeboard_${gameId}_${System.currentTimeMillis()}.jpg"
        val values = mediaStoreValues(fileName)
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
    }

    // ── Gallery ──────────────────────────────────────────────────────────────

    /**
     * Copies an image picked from the system picker into the
     * Pictures/ScoreeBoard gallery folder and returns its new URI.
     */
    fun copyToGallery(sourceUri: Uri, gameId: String): Uri? {
        val fileName = "scoreeboard_${gameId}_${System.currentTimeMillis()}.jpg"
        val targetUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaStoreValues(fileName)
        ) ?: return null

        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            targetUri
        } catch (e: Exception) {
            context.contentResolver.delete(targetUri, null, null)
            null
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    /** Removes the photo at [uriString] from MediaStore. No-op on failure. */
    fun deletePhoto(uriString: String) {
        try {
            context.contentResolver.delete(Uri.parse(uriString), null, null)
        } catch (_: Exception) { }
    }

    // ── Share image ──────────────────────────────────────────────────────────

    /**
     * Generates a 1080×1080 JPEG:
     *  - background: the game photo (center-cropped) if available,
     *    otherwise the brand pink/mint split
     *  - semi-transparent dark overlay
     *  - "ScoreeBoard" header + game title + ranked score table
     *
     * Saves the result to the app cache and returns a [FileProvider] URI
     * suitable for [Intent.ACTION_SEND].
     */
    fun createShareImage(record: GameRecord): Uri {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // ── Background ───────────────────────────────────────────────────────
        val photoBitmap = record.photoPath?.let { loadBitmap(Uri.parse(it)) }
        if (photoBitmap != null) {
            drawCenterCrop(canvas, photoBitmap, size)
            photoBitmap.recycle()
        } else {
            drawBrandBackground(canvas, size)
        }

        // ── Dark overlay ─────────────────────────────────────────────────────
        canvas.drawRect(
            0f, 0f, size.toFloat(), size.toFloat(),
            Paint().apply { color = 0xA0000000.toInt() }   // ~63 % black
        )

        // ── Text ─────────────────────────────────────────────────────────────
        val white = android.graphics.Color.WHITE
        val dimWhite = 0xCCFFFFFF.toInt()

        // App name
        canvas.drawText(
            "ScoreeBoard",
            size / 2f, 130f,
            textPaint(white, 72f, bold = true, align = Paint.Align.CENTER)
        )

        // Game title
        val displayTitle = record.title.ifBlank { "Untitled game" }
        canvas.drawText(
            displayTitle,
            size / 2f, 195f,
            textPaint(dimWhite, 42f, align = Paint.Align.CENTER)
        )

        // Divider
        canvas.drawLine(
            80f, 225f, (size - 80).toFloat(), 225f,
            Paint().apply { color = dimWhite; strokeWidth = 2f }
        )

        // Rankings
        val ranking = record.ranking()
        var y = 310f
        ranking.forEachIndexed { index, (player, score) ->
            val prefix = "${index + 1}."
            canvas.drawText(
                "$prefix  ${player.name}",
                90f, y,
                textPaint(white, 52f)
            )
            canvas.drawText(
                "$score pts",
                (size - 90).toFloat(), y,
                textPaint(white, 52f, bold = true, align = Paint.Align.RIGHT)
            )
            y += 88f
        }

        // ── Save to cache ─────────────────────────────────────────────────────
        val outDir = File(context.cacheDir, SHARE_IMAGE_DIR).also { it.mkdirs() }
        val outFile = File(outDir, "share_${record.id}.jpg")
        FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bitmap.recycle()

        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, outFile)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun mediaStoreValues(fileName: String) = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$GALLERY_FOLDER")
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? =
        try {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { null }

    private fun drawCenterCrop(canvas: Canvas, src: Bitmap, size: Int) {
        val scale = maxOf(size.toFloat() / src.width, size.toFloat() / src.height)
        val dx = (size - src.width * scale) / 2f
        val dy = (size - src.height * scale) / 2f
        val matrix = Matrix().apply { postScale(scale, scale); postTranslate(dx, dy) }
        canvas.drawBitmap(src, matrix, null)
    }

    private fun drawBrandBackground(canvas: Canvas, size: Int) {
        canvas.drawRect(0f, 0f, size / 2f, size.toFloat(),
            Paint().apply { color = 0xFFF4A8BC.toInt() })
        canvas.drawRect(size / 2f, 0f, size.toFloat(), size.toFloat(),
            Paint().apply { color = 0xFF8DCBB0.toInt() })
    }

    private fun textPaint(
        color: Int,
        textSize: Float,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.textSize = textSize
        this.textAlign = align
        this.typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        else Typeface.DEFAULT
    }
}
