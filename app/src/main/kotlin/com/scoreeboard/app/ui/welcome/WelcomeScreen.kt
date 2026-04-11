package com.scoreeboard.app.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoreeboard.app.R
import com.scoreeboard.app.ui.theme.BrandDark
import com.scoreeboard.app.ui.theme.BrandMint
import com.scoreeboard.app.ui.theme.BrandPink

@Composable
fun WelcomeScreen(
    onNewGame: () -> Unit,
    onHistory: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Vertical split background ────────────────────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(BrandPink)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(BrandMint)
            )
        }

        // ── Centered content ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )

            Spacer(Modifier.height(20.dp))

            // App name
            Text(
                text = "ScoreeBoard",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = BrandDark
            )

            Spacer(Modifier.height(6.dp))

            // Tagline
            Text(
                text = "Keep score, enjoy the game",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandDark.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(56.dp))

            // New Game button
            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "New Game",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            // History button
            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.5.dp, BrandDark),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BrandDark
                )
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
