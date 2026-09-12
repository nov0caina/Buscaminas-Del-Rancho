package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.billing.BillingManager
import com.example.games.PlayGamesManager
import com.example.ui.components.AchievementUnlockOverlay
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AnimatedSplashScreen
import com.example.ui.screens.CreditsScreen
import com.example.ui.screens.DifficultySelectionDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.PatronPassDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.RanchoTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ThemeMode

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val currentIntentState = mutableStateOf<android.content.Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntentState.value = intent
        enableEdgeToEdge()
        PlayGamesManager.getInstance(applicationContext).attachActivity(this)
        setContent {
            RanchoMinesweeperApp(
                incomingIntent = currentIntentState.value,
                onConsumeIntent = { currentIntentState.value = null }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentState.value = intent
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayGamesManager.getInstance(applicationContext).detachActivity(this)
    }
}

@Composable
fun RanchoMinesweeperApp(
    viewModel: GameViewModel = viewModel(),
    incomingIntent: android.content.Intent? = null,
    onConsumeIntent: () -> Unit = {}
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
    val unviewedAchievementIds by viewModel.unviewedAchievementIds.collectAsState()
    val isPlayGamesAuth: Boolean by viewModel.playGamesManager.isAuthenticated.collectAsState()
    val playGamesPlayerName: String? by viewModel.playGamesManager.playerName.collectAsState()

    val context = LocalContext.current
    val billingManager = remember { BillingManager.getInstance(context) }
    val isPatronUnlocked by billingManager.isPatronUnlocked.collectAsState()
    val patronPrice by billingManager.patronPrice.collectAsState()
    var showPatronPassDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDailyNotification(true)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission && uiState.isDailyNotificationEnabled) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val systemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    RanchoTheme(darkTheme = isDark) {
        val navController = rememberNavController()
        var targetScrollAchievementId by remember { mutableStateOf<String?>(null) }

        // Reactive Deep-Link / Notification Intent handler
        LaunchedEffect(incomingIntent) {
            if (incomingIntent != null) {
                val dest = incomingIntent.getStringExtra("destination")
                val achId = incomingIntent.getStringExtra("achievement_id")
                if (dest == "achievements") {
                    targetScrollAchievementId = achId
                    onConsumeIntent()
                    viewModel.startBackgroundMusic()
                    navController.navigate("achievements") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    AnimatedSplashScreen(
                        onSplashFinished = {
                            viewModel.startBackgroundMusic()
                            if (targetScrollAchievementId != null) {
                                navController.navigate("achievements") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
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
                        isPatronUnlocked = isPatronUnlocked,
                        patronPrice = patronPrice,
                        onOpenPatronPassDialog = { showPatronPassDialog = true },
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
                    val context = LocalContext.current
                    val activity = context as? Activity
                    val realGlobalEntries by viewModel.realGlobalLeaderboard.collectAsState()
                    val isLoadingGlobal by viewModel.isLoadingGlobalLeaderboard.collectAsState()

                    LeaderboardScreen(
                        isDarkTheme = isDark,
                        localScores = topScores,
                        recentMatches = topScores,
                        globalEntries = realGlobalEntries,
                        isLoadingGlobal = isLoadingGlobal,
                        isAuthenticatedPlayGames = isPlayGamesAuth,
                        playerNamePlayGames = playGamesPlayerName,
                        onRefreshGlobal = { diff, isTime ->
                            viewModel.loadGlobalLeaderboard(diff, isTime, activity)
                        },
                        onSignInPlayGames = { viewModel.playGamesManager.signIn(activity) },
                        onOpenPlayGamesLeaderboards = { diff, isTime ->
                            viewModel.playGamesManager.showLeaderboardOverlay(diff, isTime, activity)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("achievements") {
                    val context = LocalContext.current
                    val activity = context as? Activity
                    AchievementsScreen(
                        isDarkTheme = isDark,
                        achievements = achievements,
                        unviewedAchievementIds = unviewedAchievementIds,
                        targetScrollAchievementId = targetScrollAchievementId,
                        onOpenPlayGamesAchievements = { viewModel.playGamesManager.showAchievementsOverlay(activity) },
                        onDismissDetailModal = { achievementId ->
                            viewModel.markAchievementAsViewed(achievementId)
                        },
                        onBack = {
                            targetScrollAchievementId = null
                            navController.popBackStack()
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        uiState = uiState,
                        isDarkTheme = isDark,
                        isPatronUnlocked = isPatronUnlocked,
                        patronPrice = patronPrice,
                        onOpenPatronPassDialog = { showPatronPassDialog = true },
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
                onAchievementClick = { achievement ->
                    targetScrollAchievementId = achievement.id
                    navController.navigate("achievements") {
                        launchSingleTop = true
                    }
                }
            )

            // VIP Patron Pass Dialog Overlay
            if (showPatronPassDialog) {
                PatronPassDialog(
                    billingManager = billingManager,
                    onDismiss = { showPatronPassDialog = false },
                    isDarkTheme = isDark
                )
            }
        }
    }
}
