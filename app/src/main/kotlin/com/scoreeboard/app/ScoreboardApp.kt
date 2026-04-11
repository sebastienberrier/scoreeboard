package com.scoreeboard.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scoreeboard.app.navigation.GameRoute
import com.scoreeboard.app.navigation.HistoryRoute
import com.scoreeboard.app.navigation.SetupRoute
import com.scoreeboard.app.navigation.SummaryRoute
import com.scoreeboard.app.navigation.WelcomeRoute
import com.scoreeboard.app.ui.game.GameScreen
import com.scoreeboard.app.ui.history.HistoryScreen
import com.scoreeboard.app.ui.setup.SetupScreen
import com.scoreeboard.app.ui.summary.SummaryScreen
import com.scoreeboard.app.ui.welcome.WelcomeScreen
import com.scoreeboard.app.viewmodel.GameViewModel

@Composable
fun ScoreboardApp(vm: GameViewModel) {
    val navController = rememberNavController()
    val history by vm.history.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = WelcomeRoute
    ) {
        composable<WelcomeRoute> {
            WelcomeScreen(
                onNewGame = { navController.navigate(SetupRoute) },
                onHistory = { navController.navigate(HistoryRoute) }
            )
        }

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

        composable<HistoryRoute> {
            HistoryScreen(
                history = history,
                onBack = { navController.popBackStack() },
                onDeleteGame = { id -> vm.deleteGame(id) }
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
                    navController.navigate(WelcomeRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<SummaryRoute> {
            SummaryScreen(
                vm = vm,
                onNewGame = {
                    vm.newGame()
                    navController.navigate(WelcomeRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
