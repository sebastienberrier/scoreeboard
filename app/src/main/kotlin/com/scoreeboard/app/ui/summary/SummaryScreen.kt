package com.scoreeboard.app.ui.summary

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.scoreeboard.app.model.Player
import com.scoreeboard.app.ui.theme.Gold
import com.scoreeboard.app.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    vm: GameViewModel,
    onNewGame: () -> Unit
) {
    val context = LocalContext.current
    val state by vm.gameState.collectAsStateWithLifecycle()
    val photoUri by vm.currentPhotoUri.collectAsStateWithLifecycle()
    val ranking = state.ranking()
    val topBarTitle = if (state.title.isNotBlank()) "Final Scores — ${state.title}" else "Final Scores"

    // ── Pending camera URI (created before TakePicture is launched) ──────────
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // ── Camera launcher ──────────────────────────────────────────────────────
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            vm.onCameraPhotoTaken(uri)
        } else if (!success && uri != null) {
            vm.deletePendingCameraUri(uri)
        }
        pendingCameraUri = null
    }

    // ── Gallery launcher ─────────────────────────────────────────────────────
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) vm.attachPhotoFromGallery(uri)
    }

    // ── Camera permission launcher ───────────────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = vm.createCameraUri()
            if (uri != null) {
                pendingCameraUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(topBarTitle) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Winner banner ────────────────────────────────────────────────
            if (ranking.isNotEmpty()) {
                val (winner, winnerScore) = ranking.first()
                Text(
                    text = "🏆  ${winner.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$winnerScore pts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── Ranking list ─────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(ranking) { index, (player, score) ->
                    RankingRow(rank = index + 1, player = player, score = score)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // ── Photo section ────────────────────────────────────────────────
            Text(
                text = "Add a photo",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            if (photoUri != null) {
                // Preview + remove button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Game photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Text(
                        text = "Photo attached",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { vm.removePhoto() }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove photo",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                // Picker buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                val uri = vm.createCameraUri()
                                if (uri != null) {
                                    pendingCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Take photo")
                    }
                    OutlinedButton(
                        onClick = {
                            pickPhotoLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts
                                        .PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Gallery")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── New game button ───────────────────────────────────────────────
            Button(
                onClick = onNewGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Home")
            }
        }
    }
}

@Composable
private fun RankingRow(rank: Int, player: Player, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (rank == 1) {
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Gold,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("#$rank", fontWeight = FontWeight.Bold) }
        } else {
            OutlinedButton(onClick = {}, enabled = false) { Text("#$rank") }
        }
        Text(text = player.name, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f))
        Text(text = "$score pts", style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold)
    }
}
