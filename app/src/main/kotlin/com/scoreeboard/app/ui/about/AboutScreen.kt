package com.scoreeboard.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoreeboard.app.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(24.dp))

            // ── App identity ─────────────────────────────────────────────────
            Text(
                text = "ScoreeBoard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}  ·  © 2026 Seb Berrier",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "A minimalist score tracker for turn-by-turn games.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "github.com/sebastienberrier/scoreeboard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            // ── Privacy ──────────────────────────────────────────────────────
            SectionTitle("Privacy")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your data stays on your device. Nothing is collected, transmitted, or shared.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            // ── Open source libraries ────────────────────────────────────────
            SectionTitle("Open source libraries")
            Spacer(Modifier.height(10.dp))

            val libs = listOf(
                "Jetpack Compose"         to "Apache 2.0",
                "Navigation Compose"      to "Apache 2.0",
                "kotlinx.serialization"   to "Apache 2.0",
                "Lifecycle / ViewModel"   to "Apache 2.0"
            )
            libs.forEach { (name, license) ->
                LibraryRow(name = name, license = license)
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun LibraryRow(name: String, license: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = license,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
