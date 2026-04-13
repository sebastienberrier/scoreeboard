package com.scoreeboard.app.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.scoreeboard.app.model.GameRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<GameRecord>,
    onBack: () -> Unit,
    onDeleteGame: (String) -> Unit = {},
    onShareGame: (GameRecord) -> Unit = {}
) {
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val inSelectionMode = selectedIds.isNotEmpty()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (showDeleteDialog) {
        val count = selectedIds.size
        val label = if (count == 1) "1 game" else "$count games"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete $label?") },
            text = { Text("$label will be permanently removed from history.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { id -> onDeleteGame(id) }
                    selectedIds = emptySet()
                    showDeleteDialog = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inSelectionMode) "${selectedIds.size} selected" else "History") },
                navigationIcon = {
                    if (inSelectionMode) {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (inSelectionMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No games played yet.", style = MaterialTheme.typography.bodyLarge)
                Text("Finish a game to see it here.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { record ->
                    val isSelected = record.id in selectedIds
                    GameRecordCard(
                        record = record,
                        isSelected = isSelected,
                        inSelectionMode = inSelectionMode,
                        onClick = {
                            if (inSelectionMode) {
                                selectedIds = if (isSelected) selectedIds - record.id
                                             else selectedIds + record.id
                            }
                        },
                        onLongPress = { selectedIds = selectedIds + record.id },
                        onShare = { onShareGame(record) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameRecordCard(
    record: GameRecord,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onShare: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val ranking = record.ranking()
    val winner = ranking.firstOrNull()
    val displayTitle = record.title.ifBlank { "Untitled game" }
    val borderColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.border(2.dp, borderColor, MaterialTheme.shapes.medium) else Modifier)
            .combinedClickable(
                onClick = { if (inSelectionMode) onClick() else expanded = !expanded },
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ── Header: title + date ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier.size(18.dp).clip(CircleShape)
                                .background(borderColor),
                            contentAlignment = Alignment.Center
                        ) { Text("✓", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary) }
                    }
                    Text(text = displayTitle, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Subtle camera indicator
                    if (record.photoPath != null && !isSelected) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Has photo",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Text(text = dateFormat.format(Date(record.playedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Winner + round count ──────────────────────────────────────────
            if (winner != null) {
                Text(text = "🏆 ${winner.first.name}  •  ${winner.second} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "${record.rounds.size} round${if (record.rounds.size != 1) "s" else ""}  •  ${record.players.size} players",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Expanded content ──────────────────────────────────────────────
            if (!inSelectionMode && expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Photo (if any)
                if (record.photoPath != null) {
                    AsyncImage(
                        model = record.photoPath,
                        contentDescription = "Game photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Scores
                Text("Final scores", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                ranking.forEachIndexed { index, (player, total) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${index + 1}  ${player.name}",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("$total pts", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Share button
                ShareButton(record = record, onShare = onShare)
            }

            // ── Tap hint ─────────────────────────────────────────────────────
            if (!inSelectionMode) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (expanded) "Tap to collapse"
                           else "Tap to see scores  •  Hold to select",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShareButton(record: GameRecord, onShare: () -> Unit) {
    TextButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(8.dp))
        Text("Share scores")
    }
}
