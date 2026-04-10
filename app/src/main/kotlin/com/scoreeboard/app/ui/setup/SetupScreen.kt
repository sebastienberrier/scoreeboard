package com.scoreeboard.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onStartGame: (title: String, playerNames: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var playerCount by remember { mutableIntStateOf(MIN_PLAYERS) }

    // One mutable state per player slot; list is recreated when playerCount changes.
    val playerNames = remember(playerCount) {
        List(playerCount) { mutableStateOf("") }
    }

    val canStart = playerNames.all { it.value.isNotBlank() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("New Game") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Game title (optional) ────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Game title (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── Player count stepper ─────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Players:", modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { if (playerCount > MIN_PLAYERS) playerCount-- },
                    enabled = playerCount > MIN_PLAYERS
                ) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = playerCount.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { if (playerCount < MAX_PLAYERS) playerCount++ },
                    enabled = playerCount < MAX_PLAYERS
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add player")
                }
            }

            // ── Player name fields ───────────────────────────────────────
            playerNames.forEachIndexed { index, nameState ->
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text("Player ${index + 1}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = if (index < playerCount - 1) ImeAction.Next else ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Start button ─────────────────────────────────────────────
            Button(
                onClick = {
                    onStartGame(title, playerNames.map { it.value })
                },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Game")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
