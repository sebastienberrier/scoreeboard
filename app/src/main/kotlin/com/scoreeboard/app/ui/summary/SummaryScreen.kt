package com.scoreeboard.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scoreeboard.app.model.Player
import com.scoreeboard.app.ui.theme.Gold
import com.scoreeboard.app.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    vm: GameViewModel,
    onNewGame: () -> Unit
) {
    val state by vm.gameState.collectAsStateWithLifecycle()
    val ranking = state.ranking()
    val topBarTitle = if (state.title.isNotBlank()) "Final Scores — ${state.title}" else "Final Scores"

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

            // ── Winner banner ────────────────────────────────────────────
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

            // ── Ranking list ─────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(ranking) { index, (player, score) ->
                    RankingRow(
                        rank = index + 1,
                        player = player,
                        score = score
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── New game button ──────────────────────────────────────────
            Button(
                onClick = onNewGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("New Game")
            }
        }
    }
}

@Composable
private fun RankingRow(
    rank: Int,
    player: Player,
    score: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank badge: gold filled for 1st, outlined for others
        if (rank == 1) {
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Gold,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("#$rank", fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(onClick = {}, enabled = false) {
                Text("#$rank")
            }
        }

        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$score pts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
