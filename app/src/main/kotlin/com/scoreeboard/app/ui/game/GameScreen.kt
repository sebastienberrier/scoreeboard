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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    onEndGame: () -> Unit
) {
    val state by vm.gameState.collectAsStateWithLifecycle()
    val draftScores by vm.draftScores.collectAsStateWithLifecycle()
    val isDraftValid by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf { vm.isDraftValid() }
    }

    val topBarTitle = state.title.ifBlank { "scoreeboard" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                actions = {
                    IconButton(onClick = onEndGame) {
                        Icon(Icons.Default.Close, contentDescription = "End game")
                    }
                }
            )
        }
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
                    .padding(8.dp)
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

    // Auto-scroll to the latest round whenever a new one is added.
    LaunchedEffect(state.rounds.size) {
        if (state.rounds.isNotEmpty()) {
            listState.animateScrollToItem(state.rounds.size - 1)
        }
    }

    Column(modifier = modifier) {
        // ── Header row ───────────────────────────────────────────────────
        TableRow(
            label = "Round",
            players = state.players,
            getValue = { p -> p.name },
            isHeader = true
        )

        HorizontalDivider()

        // ── Per-round rows ───────────────────────────────────────────────
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

        // ── Totals row ───────────────────────────────────────────────────
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
