package com.scoreeboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scoreeboard.app.data.GamesRepository
import com.scoreeboard.app.ui.theme.ScoreboardTheme
import com.scoreeboard.app.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Repository is created once per Activity; the ViewModel lambda is only
        // called on first creation, so the same instance is reused across rotations.
        val repository = GamesRepository(applicationContext)

        setContent {
            ScoreboardTheme {
                val vm: GameViewModel = viewModel { GameViewModel(repository) }
                ScoreboardApp(vm)
            }
        }
    }
}
