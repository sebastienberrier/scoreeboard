package com.scoreeboard.app

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scoreeboard.app.navigation.AboutRoute
import com.scoreeboard.app.navigation.GameRoute
import com.scoreeboard.app.navigation.HistoryRoute
import com.scoreeboard.app.navigation.ResumeRoute
import com.scoreeboard.app.navigation.SetupRoute
import com.scoreeboard.app.navigation.SummaryRoute
import com.scoreeboard.app.navigation.WelcomeRoute
import com.scoreeboard.app.ui.about.AboutScreen
import com.scoreeboard.app.ui.game.GameScreen
import com.scoreeboard.app.ui.history.HistoryScreen
import com.scoreeboard.app.ui.resume.ResumeScreen
import com.scoreeboard.app.ui.setup.SetupScreen
import com.scoreeboard.app.ui.summary.SummaryScreen
import com.scoreeboard.app.ui.welcome.WelcomeScreen
import com.scoreeboard.app.viewmodel.GameViewModel

@Composable
fun ScoreboardApp(vm: GameViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val history by vm.history.collectAsStateWithLifecycle()
    val drafts  by vm.drafts.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = WelcomeRoute
    ) {
        composable<WelcomeRoute> {
            WelcomeScreen(
                onNewGame = { navController.navigate(SetupRoute) },
                onResume  = { navController.navigate(ResumeRoute) },
                hasDrafts = drafts.isNotEmpty(),
                onHistory = { navController.navigate(HistoryRoute) },
                onAbout   = { navController.navigate(AboutRoute) }
            )
        }

        composable<AboutRoute> {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable<ResumeRoute> {
            ResumeScreen(
                drafts   = drafts,
                onResume = { draft ->
                    vm.resumeGame(draft)
                    navController.navigate(GameRoute) {
                        popUpTo(ResumeRoute) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
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
                history      = history,
                onBack       = { navController.popBackStack() },
                onDeleteGame = { id -> vm.deleteGame(id) },
                onShareGame  = { record ->
                    scope.launch {
                        val shareUri = withContext(Dispatchers.IO) {
                            vm.createShareImage(record)
                        }
                        if (shareUri != null) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share scores"))
                        }
                    }
                }
            )
        }

        composable<GameRoute> {
            GameScreen(
                vm          = vm,
                onEndGame   = {
                    navController.navigate(SummaryRoute) {
                        popUpTo(GameRoute) { inclusive = true }
                    }
                },
                onAbortGame = {
                    navController.navigate(WelcomeRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onResumeLater = {
                    navController.navigate(WelcomeRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<SummaryRoute> {
            SummaryScreen(
                vm        = vm,
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
