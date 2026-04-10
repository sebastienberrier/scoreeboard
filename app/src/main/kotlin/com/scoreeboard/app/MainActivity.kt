package com.scoreeboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scoreeboard.app.ui.theme.ScoreboardTheme
import com.scoreeboard.app.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScoreboardTheme {
                // The ViewModel is Activity-scoped: all three screens share
                // the same instance and survive configuration changes.
                val vm: GameViewModel = viewModel()
                ScoreboardApp(vm)
            }
        }
    }
}
