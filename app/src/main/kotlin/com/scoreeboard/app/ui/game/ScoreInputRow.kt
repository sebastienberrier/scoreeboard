package com.scoreeboard.app.ui.game

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.scoreeboard.app.model.Player

/**
 * One row in the "Add Round" card: player name on the left, score field on the right.
 *
 * Extracted as a separate composable so Compose can skip unchanged rows
 * during recomposition (stable parameters).
 */
@Composable
fun ScoreInputRow(
    player: Player,
    value: String,
    onValueChange: (String) -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = player.name,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Score") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,   // allows minus sign for negative scores
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            modifier = Modifier.width(120.dp)
        )
    }
}
