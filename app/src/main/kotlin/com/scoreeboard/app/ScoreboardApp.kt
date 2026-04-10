package com.scoreeboard.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scoreeboard.app.navigation.GameRoute
import com.scoreeboard.app.navigation.SetupRoute
import com.scoreeboard.app.navigation.SummaryRoute
import com.scoreeboard.app.ui.game.GameScreen
import com.scoreeboard.app.ui.setup.SetupScreen
import com.scoreeboard.app.ui.summary.SummaryScreen
import com.scoreeboard.app.viewmodel.GameViewModel

/**
 * Root composable: owns the NavController and wires the three screens to the
 * shared [GameViewModel].
 *
 * Navigation strategy: each transition clears its own destination from the
 * back-stack with [popUpTo] { inclusive = true }, so the Android back button
 * never returns to a stale screen. From [GameRoute] it exits the app (correct
 * behaviour — the user should not lose the game by accident).
 */
@Composable
fun ScoreboardApp(vm: GameViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SetupRoute
    ) {
        composable<SetupRoute> {
            SetupScreen(
                onStartGame = { title, names ->
                    vm.startGame(title, names)
                    navController.navigate(GameRoute) {
                        popUpTo(SetupRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<GameRoute> {
            GameScreen(
                vm = vm,
                onEndGame = {
                    navController.navigate(SummaryRoute) {
                        popUpTo(GameRoute) { inclusive = true }
                    }
                },
                onAbortGame = {
                    navController.navigate(SetupRoute) {
                        popUpTo(GameRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<SummaryRoute> {
            SummaryScreen(
                vm = vm,
                onNewGame = {
                    vm.newGame()
                    navController.navigate(SetupRoute) {
                        popUpTo(SummaryRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}
