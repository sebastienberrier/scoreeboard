package com.scoreeboard.app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scoreeboard.app.model.GameState
import com.scoreeboard.app.model.Player
import com.scoreeboard.app.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    vm: GameViewModel,
    onEndGame: () -> Unit,
    onAbortGame: () -> Unit
) {
    val state by vm.gameState.collectAsStateWithLifecycle()
    val draftScores by vm.draftScores.collectAsStateWithLifecycle()
    val isDraftValid by remember { derivedStateOf { vm.isDraftValid() } }

    var showEndDialog   by remember { mutableStateOf(false) }
    var showAbortDialog by remember { mutableStateOf(false) }

    val topBarTitle = state.title.ifBlank { "scoreeboard" }

    // ── End game confirmation dialog ─────────────────────────────────────────
    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End game?") },
            text  = { Text("This will display the final scores and declare a winner.") },
            confirmButton = {
                Button(onClick = {
                    showEndDialog = false
                    vm.endGame()
                    onEndGame()
                }) { Text("End Game") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Abort game confirmation dialog ───────────────────────────────────────
    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text("Abort game?") },
            text  = { Text("All scores will be lost. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showAbortDialog = false
                        vm.abortGame()
                        onAbortGame()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Abort") }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(topBarTitle) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Scoreboard table ─────────────────────────────────────────
            ScoreboardTable(
                state = state,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            HorizontalDivider()

            // ── Round input card ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Round ${state.rounds.size + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    state.players.forEachIndexed { index, player ->
                        ScoreInputRow(
                            player = player,
                            value = draftScores[player.id] ?: "",
                            onValueChange = { vm.updateDraftScore(player.id, it) },
                            isLast = index == state.players.lastIndex
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = { vm.submitRound() },
                        enabled = isDraftValid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Round")
                    }
                }
            }

            // ── End / Abort buttons ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showAbortDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Abort Game")
                }
                Button(
                    onClick = { showEndDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("End Game")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Scoreboard table ─────────────────────────────────────────────────────────

@Composable
private fun ScoreboardTable(
    state: GameState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.rounds.size) {
        if (state.rounds.isNotEmpty()) {
            listState.animateScrollToItem(state.rounds.size - 1)
        }
    }

    Column(modifier = modifier) {
        TableRow(
            label = "Round",
            players = state.players,
            getValue = { p -> p.name },
            isHeader = true
        )

        HorizontalDivider()

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items(state.rounds, key = { it.number }) { round ->
                TableRow(
                    label = round.number.toString(),
                    players = state.players,
                    getValue = { p -> (round.scores[p.id] ?: 0).toString() }
                )
            }
        }

        HorizontalDivider()

        TableRow(
            label = "Total",
            players = state.players,
            getValue = { p -> state.totalFor(p.id).toString() },
            isBold = true,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun TableRow(
    label: String,
    players: List<Player>,
    getValue: (Player) -> String,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    isBold: Boolean = false
) {
    val style = when {
        isHeader -> MaterialTheme.typography.titleSmall
        isBold   -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        else     -> MaterialTheme.typography.bodyMedium
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Box(Modifier.weight(1f)) {
            Text(text = label, style = style)
        }
        players.forEach { player ->
            Text(
                text = getValue(player),
                style = style,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
