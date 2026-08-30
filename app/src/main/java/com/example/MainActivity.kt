package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AchievementUnlockOverlay
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AnimatedSplashScreen
import com.example.ui.screens.CreditsScreen
import com.example.ui.screens.DifficultySelectionDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.RanchoTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ThemeMode

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RanchoMinesweeperApp()
        }
    }
}

@Composable
fun RanchoMinesweeperApp(
    viewModel: GameViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                    viewModel.resumeBackgroundMusic()
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.pauseBackgroundMusic()
                    viewModel.autoSaveActiveGame()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val topScores by viewModel.topScores.collectAsState()
    val achievements by viewModel.allAchievements.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    RanchoTheme(darkTheme = isDark) {
        val navController = rememberNavController()

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    AnimatedSplashScreen(
                        onSplashFinished = {
                            viewModel.startBackgroundMusic()
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        viewModel.checkSavedGameAvailable()
                    }
                    HomeScreen(
                        uiState = uiState,
                        isDarkTheme = isDark,
                        onResumeGame = {
                            viewModel.resumeSavedGame()
                            navController.navigate("game")
                        },
                        onSelectDifficulty = { difficulty, customRows, customCols, customMines ->
                            viewModel.startNewGame(
                                difficulty = difficulty,
                                customRows = customRows,
                                customCols = customCols,
                                customMines = customMines
                            )
                            navController.navigate("game")
                        },
                        onLeaderboardClick = {
                            navController.navigate("leaderboard")
                        },
                        onAchievementsClick = {
                            navController.navigate("achievements")
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                }

                composable("game") {
                    GameScreen(
                        uiState = uiState,
                        isDarkTheme = isDark,
                        onCellClick = { r, c, pressure -> viewModel.onCellClick(r, c, pressure) },
                        onCellLongClick = { r, c -> viewModel.onCellLongClick(r, c) },
                        onCellChord = { r, c -> viewModel.onCellChord(r, c) },
                        onResetGame = { viewModel.startNewGame(uiState.difficulty, uiState.rows, uiState.cols, uiState.mines) },
                        onBackToMenu = {
                            viewModel.autoSaveActiveGame()
                            viewModel.checkSavedGameAvailable()
                            navController.popBackStack()
                        }
                    )
                }

                composable("leaderboard") {
                    LeaderboardScreen(
                        isDarkTheme = isDark,
                        localScores = topScores,
                        globalEntries = viewModel.getGlobalLeaderboard(),
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("achievements") {
                    AchievementsScreen(
                        isDarkTheme = isDark,
                        achievements = achievements,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        uiState = uiState,
                        isDarkTheme = isDark,
                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                        onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                        onToggleHaptics = { viewModel.setHaptics(it) },
                        onToggleDailyNotification = { viewModel.setDailyNotification(it) },
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onMusicVolumeChange = { viewModel.setMusicVolume(it) },
                        onToggleSfx = { viewModel.setSfxEnabled(it) },
                        onSfxVolumeChange = { viewModel.setSfxVolume(it) },
                        onSelectRanchFlagIcon = { viewModel.setRanchFlagIcon(it) },
                        onNavigateToCredits = { navController.navigate("credits") },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("credits") {
                    CreditsScreen(
                        isDarkTheme = isDark,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Global Achievement Unlock Notification Overlay
            AchievementUnlockOverlay(
                unlockEvents = viewModel.achievementUnlockEvents,
                onAchievementClick = {
                    navController.navigate("achievements")
                }
            )
        }
    }
}
