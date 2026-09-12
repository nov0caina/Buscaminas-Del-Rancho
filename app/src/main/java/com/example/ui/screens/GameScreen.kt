package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CellState
import com.example.data.model.ClickMode
import com.example.data.model.GameStatus
import com.example.data.model.RanchFlagIcon
import com.example.data.model.RevealCluster
import com.example.data.model.VaqueroFace
import com.example.ui.components.AnimatedRanchFlagEmoji
import com.example.ui.components.RanchBoardFrame
import com.example.ui.components.RanchTactileCell
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.ExplosionParticleSystem
import com.example.ui.theme.BoardRivetGold
import com.example.ui.theme.BoardWoodBorderDark
import com.example.ui.theme.BoardWoodBorderLight
import com.example.ui.theme.BoardWoodSurfaceDark
import com.example.ui.theme.BoardWoodSurfaceLight
import com.example.ui.theme.Number1Blue
import com.example.ui.theme.Number2Green
import com.example.ui.theme.Number3Red
import com.example.ui.theme.Number4Purple
import com.example.ui.theme.Number5Maroon
import com.example.ui.theme.Number6Teal
import com.example.ui.theme.Number7Black
import com.example.ui.theme.Number8Gray
import com.example.ui.viewmodel.GameUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onCellClick: (row: Int, col: Int, pressure: Float) -> Unit,
    onCellLongClick: (row: Int, col: Int) -> Unit,
    onCellChord: (row: Int, col: Int) -> Unit,
    onResetGame: () -> Unit,
    onBackToMenu: () -> Unit,
    onToggleClickMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showEndDialog by remember { mutableStateOf(false) }

    // Screen Shake effect on dynamite explosion
    val shakeProgress = remember { Animatable(0f) }
    LaunchedEffect(uiState.explosionEventId) {
        if (uiState.explosionEventId != 0L && uiState.gameStatus == GameStatus.LOST) {
            shakeProgress.snapTo(1f)
            shakeProgress.animateTo(0f, tween(durationMillis = 480, easing = LinearEasing))
        }
    }
    val shakeVal = shakeProgress.value
    val shakeOffsetX = if (shakeVal > 0.01f) (kotlin.math.sin(shakeVal * 42f) * 18f * shakeVal) else 0f
    val shakeOffsetY = if (shakeVal > 0.01f) (kotlin.math.cos(shakeVal * 36f) * 14f * shakeVal) else 0f

    // Smooth timing for End Game Dialog to allow full visual animation to play
    LaunchedEffect(uiState.gameStatus, uiState.explosionEventId, uiState.victoryEventId) {
        when (uiState.gameStatus) {
            GameStatus.WON -> {
                showEndDialog = false
                delay(1400) // Dejar que la fiesta de confeti y fuegos artificiales se aprecie
                showEndDialog = true
            }
            GameStatus.LOST -> {
                showEndDialog = false
                delay(1400) // Dejar que la gran explosión y humo de pólvora se desplieguen primero
                showEndDialog = true
            }
            else -> {
                showEndDialog = false
            }
        }
    }

    LaunchedEffect(uiState.rows, uiState.cols) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mina ${uiState.difficulty.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToMenu,
                        modifier = Modifier.testTag("btn_game_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Menú"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onResetGame,
                        modifier = Modifier.testTag("btn_game_reset_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reiniciar Game"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uiState.gameStatus == GameStatus.PLAYING || uiState.gameStatus == GameStatus.IDLE) {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⛏️ Clic simple: Revelar  •  ${uiState.ranchFlagIcon.emoji} Mantén presionado: Poner ${uiState.ranchFlagIcon.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game HUD Header (Mines counter, Vaquero face, Timer)
                GameHudHeader(
                    minesLeft = (uiState.mines - uiState.flagsPlaced).coerceAtLeast(-99),
                    vaqueroFace = uiState.vaqueroFace,
                    timeElapsed = uiState.timeElapsed,
                    isDarkTheme = isDarkTheme,
                    onFaceClick = onResetGame
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Minefield Grid container with free panning and zoom gestures
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    val availableWidth = (maxWidth - 12.dp).coerceAtLeast(100.dp)
                    val availableHeight = (maxHeight - 12.dp).coerceAtLeast(100.dp)

                    val density = LocalDensity.current

                    // Cálculo dinámico para optimizar el tamaño de celdas en pantallas alargadas
                    val framePadding = 12.dp
                    val framePaddingTotal = framePadding * 2 // 24.dp simétrico
                    val spacingDp = 2.dp
                    val spacingTotalX = spacingDp * (uiState.cols - 1)
                    val spacingTotalY = spacingDp * (uiState.rows - 1)

                    val maxCellWidth = (availableWidth - framePaddingTotal - spacingTotalX) / uiState.cols
                    val maxCellHeight = (availableHeight - framePaddingTotal - spacingTotalY) / uiState.rows
                    val maxPossibleCell = minOf(maxCellWidth, maxCellHeight)

                    val rawCellDp = when {
                        uiState.cols <= 9 -> maxPossibleCell.coerceIn(34.dp, 44.dp)
                        uiState.cols <= 12 -> maxPossibleCell.coerceIn(28.dp, 36.dp)
                        else -> maxPossibleCell.coerceIn(24.dp, 32.dp)
                    }

                    // Cuantización a píxeles enteros para garantizar coherencia dimensional idéntica en todas las columnas
                    val cellPx = with(density) { rawCellDp.roundToPx() }
                    val spacingPx = with(density) { spacingDp.roundToPx() }
                    val cellDp = with(density) { cellPx.toDp() }

                    val gridWidthPx = (cellPx * uiState.cols) + (spacingPx * (uiState.cols - 1))
                    val gridHeightPx = (cellPx * uiState.rows) + (spacingPx * (uiState.rows - 1))
                    val gridWidth = with(density) { gridWidthPx.toDp() }
                    val gridHeight = with(density) { gridHeightPx.toDp() }

                    val boardWidth = gridWidth + framePaddingTotal
                    val boardHeight = gridHeight + framePaddingTotal

                    val fitScaleX = availableWidth / boardWidth
                    val fitScaleY = availableHeight / boardHeight
                    val fitScale = minOf(fitScaleX, fitScaleY).coerceIn(0.15f, 1f)

                    LaunchedEffect(uiState.rows, uiState.cols, maxWidth, maxHeight) {
                        scale = fitScale
                        offsetX = 0f
                        offsetY = 0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    var pastTouchSlop = false
                                    val touchSlop = viewConfiguration.touchSlop

                                    awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)

                                    do {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                        val pointerCount = event.changes.count { it.pressed }

                                        if (pointerCount >= 2) {
                                            val zoomChange = event.calculateZoom()
                                            val panChange = event.calculatePan()

                                            val zoomMotion = kotlin.math.abs(1f - zoomChange)
                                            val panMotion = panChange.getDistance()

                                            if (!pastTouchSlop && (zoomMotion > 0.01f || panMotion > touchSlop)) {
                                                pastTouchSlop = true
                                            }

                                            if (pastTouchSlop) {
                                                if (zoomChange != 1f) {
                                                    scale = (scale * zoomChange).coerceIn(fitScale * 0.7f, 4.0f)
                                                }
                                                if (panChange != Offset.Zero) {
                                                    offsetX += panChange.x
                                                    offsetY += panChange.y
                                                }
                                                event.changes.forEach { change ->
                                                    if (change.pressed) {
                                                        change.consume()
                                                    }
                                                }
                                            }
                                        } else if (pointerCount == 1 && scale > fitScale * 1.05f) {
                                            val panChange = event.calculatePan()
                                            val panMotion = panChange.getDistance()

                                            if (!pastTouchSlop && panMotion > touchSlop) {
                                                pastTouchSlop = true
                                            }

                                            if (pastTouchSlop && panChange != Offset.Zero) {
                                                offsetX += panChange.x
                                                offsetY += panChange.y
                                                event.changes.forEach { change ->
                                                    if (change.pressed) {
                                                        change.consume()
                                                    }
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        RanchBoardFrame(
                            isDarkTheme = isDarkTheme,
                            framePadding = framePadding,
                            modifier = Modifier
                                .requiredSize(width = boardWidth, height = boardHeight)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX + shakeOffsetX,
                                    translationY = offsetY + shakeOffsetY
                                )
                        ) {
                            MinefieldGrid(
                                rows = uiState.rows,
                                cols = uiState.cols,
                                grid = uiState.grid,
                                activeRevealCluster = uiState.activeRevealCluster,
                                ranchFlagIcon = uiState.ranchFlagIcon,
                                cellDp = cellDp,
                                isDarkTheme = isDarkTheme,
                                onCellClick = onCellClick,
                                onCellLongClick = onCellLongClick,
                                onCellChord = onCellChord
                            )
                        }
                    }

                    val isViewingBoard = !showEndDialog && (uiState.gameStatus == GameStatus.WON || uiState.gameStatus == GameStatus.LOST)
                    val fabBottomPadding by animateDpAsState(
                        targetValue = if (isViewingBoard) 80.dp else 12.dp,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "fab_bottom_padding"
                    )

                    // Floating Centering Control (Campirano con remache dorado)
                    SmallFloatingActionButton(
                        onClick = {
                            scale = fitScale
                            offsetX = 0f
                            offsetY = 0f
                        },
                        containerColor = if (isDarkTheme) BoardWoodSurfaceDark else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isDarkTheme) BoardRivetGold else MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = fabBottomPadding)
                            .border(1.5.dp, BoardRivetGold.copy(alpha = 0.6f), CircleShape)
                            .testTag("btn_zoom_reset")
                    ) {
                        Text("🎯", fontSize = 16.sp)
                    }
                }
            }

            // Gran Explosión y Humo Denso que Abarca Toda la Pantalla al Perder
            if (uiState.gameStatus == GameStatus.LOST && uiState.explosionEventId != 0L) {
                key(uiState.explosionEventId) {
                    MassiveScreenExplosionOverlay(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Gran Celebración Campirana de Victoria con Confeti, Rayos de Sol y Destellos
            if (uiState.gameStatus == GameStatus.WON && uiState.victoryEventId != 0L) {
                key(uiState.victoryEventId) {
                    VictoryFiestaOverlay(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // End-of-game victory/loss overlay dialog or floating button
            if (uiState.gameStatus == GameStatus.WON || uiState.gameStatus == GameStatus.LOST) {
                if (showEndDialog) {
                    GameEndOverlayDialog(
                        gameStatus = uiState.gameStatus,
                        timeElapsed = uiState.timeElapsed,
                        difficultyName = uiState.difficulty.displayName,
                        isDarkTheme = isDarkTheme,
                        onPlayAgain = onResetGame,
                        onBackToMenu = onBackToMenu,
                        onViewBoard = { showEndDialog = false }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF2C221E) else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Ver Resultado button
                            Box(
                                modifier = Modifier
                                    .bounceClick(onClick = { showEndDialog = true })
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(bottom = 3.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ver Resultado",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Reintentar button
                            Box(
                                modifier = Modifier
                                    .bounceClick(onClick = onResetGame)
                                    .background(Color.Black.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                                    .padding(bottom = 3.dp)
                                    .background(
                                        if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reintentar",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHudHeader(
    minesLeft: Int,
    vaqueroFace: VaqueroFace,
    timeElapsed: Int,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onFaceClick: () -> Unit
) {
    val woodBorder = if (isDarkTheme) BoardWoodBorderDark else BoardWoodBorderLight
    val woodSurface = if (isDarkTheme) BoardWoodSurfaceDark else BoardWoodSurfaceLight
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .shadow(6.dp, shape)
            .border(2.dp, woodBorder, shape),
        shape = shape,
        color = woodSurface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mines Counter Display
            HudCounterBox(
                emoji = "🧨",
                valueString = String.format("%03d", minesLeft),
                isDarkTheme = isDarkTheme,
                testTag = "hud_mines_counter"
            )

            // Vaquero Reset Face Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .shadow(4.dp, CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        CircleShape
                    )
                    .border(2.dp, BoardRivetGold, CircleShape)
                    .clickable(onClick = onFaceClick)
                    .testTag("hud_vaquero_face"),
                contentAlignment = Alignment.Center
            ) {
                val faceEmoji = when (vaqueroFace) {
                    VaqueroFace.HAPPY -> "🤠"
                    VaqueroFace.SUSPENSE -> "😲"
                    VaqueroFace.DEAD -> "😵"
                    VaqueroFace.VICTORIOUS -> "🏆"
                }
                Text(
                    text = faceEmoji,
                    fontSize = 28.sp
                )
            }

            // Timer Display
            val mins = timeElapsed / 60
            val secs = timeElapsed % 60
            HudCounterBox(
                emoji = "⏱️",
                valueString = String.format("%02d:%02d", mins, secs),
                isDarkTheme = isDarkTheme,
                testTag = "hud_timer"
            )
        }
    }
}

@Composable
private fun HudCounterBox(
    emoji: String,
    valueString: String,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDarkTheme) Color(0xFF16120F) else Color(0xFF2C1D14),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.4f)),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = valueString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFB300),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MinefieldGrid(
    rows: Int,
    cols: Int,
    grid: List<CellState>,
    activeRevealCluster: RevealCluster?,
    ranchFlagIcon: RanchFlagIcon,
    cellDp: Dp,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onCellClick: (row: Int, col: Int, pressure: Float) -> Unit,
    onCellLongClick: (row: Int, col: Int) -> Unit,
    onCellChord: (row: Int, col: Int) -> Unit
) {
    val density = LocalDensity.current
    val cellPx = with(density) { cellDp.roundToPx() }
    val spacingPx = with(density) { 2.dp.roundToPx() }
    val gridWidthPx = (cellPx * cols) + (spacingPx * (cols - 1))
    val gridHeightPx = (cellPx * rows) + (spacingPx * (rows - 1))
    val gridWidth = with(density) { gridWidthPx.toDp() }
    val gridHeight = with(density) { gridHeightPx.toDp() }

    Box(
        modifier = Modifier.requiredSize(width = gridWidth, height = gridHeight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (r in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (c in 0 until cols) {
                        val index = r * cols + c
                        if (index in grid.indices) {
                            RanchTactileCell(
                                cell = grid[index],
                                cellDp = cellDp,
                                ranchFlagIcon = ranchFlagIcon,
                                isDarkTheme = isDarkTheme,
                                onClick = { pressure -> onCellClick(r, c, pressure) },
                                onLongClick = { onCellLongClick(r, c) },
                                onChord = { onCellChord(r, c) }
                            )
                        }
                    }
                }
            }
        }

        // Gran Animación Unificada de Polvo que Cubre Toda la Extensión Desbloqueada
        activeRevealCluster?.let { cluster ->
            key(cluster.id) {
                MassiveDesertDustOverlay(
                    cluster = cluster,
                    cellDp = cellDp,
                    spacingDp = 2.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Animación Grande y Unificada de Polvareda del Desierto optimizada para alto rendimiento (60/120 FPS)
 * utilizando un Object Pool de partículas reutilizables.
 */
@Composable
private fun MassiveDesertDustOverlay(
    cluster: RevealCluster,
    cellDp: Dp,
    spacingDp: Dp,
    modifier: Modifier = Modifier
) {
    val dustAnim = remember { Animatable(0f) }
    val dustSystem = remember { DustParticleSystem(capacity = 48) }

    LaunchedEffect(cluster.id) {
        dustAnim.snapTo(0f)
        dustAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing)
        )
    }

    val globalProgress = dustAnim.value
    if (globalProgress < 1f) {
        val density = LocalDensity.current
        val cellStepPx = with(density) { (cellDp + spacingDp).toPx() }
        val cellDpPx = with(density) { cellDp.toPx() }

        val minX = cluster.minCol * cellStepPx
        val minY = cluster.minRow * cellStepPx
        val maxX = cluster.maxCol * cellStepPx + cellDpPx
        val maxY = cluster.maxRow * cellStepPx + cellDpPx

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val originX = cluster.originCol * cellStepPx + cellDpPx / 2f
        val originY = cluster.originRow * cellStepPx + cellDpPx / 2f

        val spanW = (maxX - minX).coerceAtLeast(cellDpPx)
        val spanH = (maxY - minY).coerceAtLeast(cellDpPx)
        val maxSpan = kotlin.math.max(spanW, spanH)

        // Configuración inicial en el Object Pool (cero asignaciones en cada frame posterior)
        dustSystem.setupCluster(
            clusterId = cluster.id,
            cellCount = cluster.cellCount,
            originX = originX,
            originY = originY,
            centerX = centerX,
            centerY = centerY,
            spanW = spanW,
            spanH = spanH,
            maxSpan = maxSpan,
            cellDpPx = cellDpPx
        )

        Canvas(modifier = modifier) {
            dustSystem.render(this, globalProgress)
        }
    }
}

/**
 * Gran Animación de Explosión y Humo Volumétrico Optimizada para alto rendimiento (60/120 FPS)
 * utilizando un Object Pool de partículas reutilizables.
 */
@Composable
private fun MassiveScreenExplosionOverlay(
    modifier: Modifier = Modifier
) {
    val explosionAnim = remember { Animatable(0f) }
    val explosionSystem = remember { ExplosionParticleSystem(capacity = 64) }

    LaunchedEffect(Unit) {
        explosionAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val progress = explosionAnim.value
    if (progress < 1f) {
        Canvas(modifier = modifier) {
            val maxScreenRadius = kotlin.math.max(size.width, size.height) * 0.8f
            explosionSystem.setupExplosion(size.width / 2f, size.height / 2f, maxScreenRadius)
            explosionSystem.render(this, progress, size.width, size.height)
        }
    }
}

/**
 * Gran Celebración Campirana de Victoria con lluvia de confeti multicolor,
 * rayos de sol dorados, fuegos artificiales campiranos y emojis festivos flotantes.
 */
@Composable
private fun VictoryFiestaOverlay(
    modifier: Modifier = Modifier
) {
    val fiestaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        fiestaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    val progress = fiestaAnim.value
    if (progress < 1f) {
        val fiestaAlpha = if (progress < 0.15f) {
            progress / 0.15f
        } else if (progress > 0.82f) {
            (1f - progress) / 0.18f
        } else {
            1f
        }.coerceIn(0f, 1f)

        Box(modifier = modifier.clipToBounds()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val topCenter = Offset(size.width / 2f, 0f)
                val maxDim = kotlin.math.max(size.width, size.height)

                // 1. RESPLANDOR DORADO DE TRIUNFO EN EL FONDO (0% -> 40%)
                if (progress < 0.5f) {
                    val burstProgress = progress / 0.5f
                    val burstAlpha = (1f - burstProgress) * fiestaAlpha * 0.35f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD166).copy(alpha = burstAlpha),
                                Color(0xFFE9C46A).copy(alpha = burstAlpha * 0.6f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = maxDim * 0.75f * (0.3f + burstProgress * 0.7f)
                        ),
                        radius = maxDim * 0.75f * (0.3f + burstProgress * 0.7f),
                        center = center
                    )
                }

                // 2. RAYOS DE SOL DORADOS GIRATORIOS
                val rayCount = 12
                val rayAlpha = fiestaAlpha * 0.18f * (1f - progress * 0.4f)
                if (rayAlpha > 0.01f) {
                    for (r in 0 until rayCount) {
                        val angleDeg = (r * (360.0 / rayCount)) + (progress * 45.0)
                        val rad = Math.toRadians(angleDeg)
                        val rayEnd = center + Offset(
                            x = (maxDim * kotlin.math.cos(rad)).toFloat(),
                            y = (maxDim * kotlin.math.sin(rad)).toFloat()
                        )
                        drawLine(
                            color = Color(0xFFFFD166).copy(alpha = rayAlpha),
                            start = center,
                            end = rayEnd,
                            strokeWidth = 24f * (1f - progress * 0.3f)
                        )
                    }
                }

                // 3. FUEGOS ARTIFICIALES CAMPIRANOS (COHETES DE FIESTA)
                val fireworkOrigins = listOf(
                    Offset(size.width * 0.22f, size.height * 0.28f),
                    Offset(size.width * 0.78f, size.height * 0.22f),
                    Offset(size.width * 0.50f, size.height * 0.18f)
                )

                fireworkOrigins.forEachIndexed { fIdx, fCenter ->
                    val fStartProgress = fIdx * 0.15f
                    if (progress >= fStartProgress) {
                        val fLocalProgress = ((progress - fStartProgress) / 0.55f).coerceIn(0f, 1f)
                        val fAlpha = (1f - fLocalProgress) * fiestaAlpha
                        if (fAlpha > 0.01f) {
                            val fSparks = 18
                            val fRadius = (size.width * 0.32f) * fLocalProgress
                            for (s in 0 until fSparks) {
                                val sRad = Math.toRadians(s * (360.0 / fSparks) + (fIdx * 15.0))
                                val sPos = fCenter + Offset(
                                    x = (fRadius * kotlin.math.cos(sRad)).toFloat(),
                                    y = (fRadius * kotlin.math.sin(sRad)).toFloat() + (fLocalProgress * fLocalProgress * 45f)
                                )
                                val sparkColor = when ((s + fIdx) % 5) {
                                    0 -> Color(0xFFFFD166) // Dorado
                                    1 -> Color(0xFFEF476F) // Rosa Mexicano
                                    2 -> Color(0xFF06D6A0) // Verde Esmeralda
                                    3 -> Color(0xFF118AB2) // Azul Cielo
                                    else -> Color(0xFFF4A261) // Naranja Campirano
                                }
                                drawCircle(
                                    color = sparkColor.copy(alpha = fAlpha * 0.95f),
                                    radius = (4.5f * (1f - fLocalProgress)).coerceAtLeast(1.2f),
                                    center = sPos
                                )
                            }
                        }
                    }
                }

                // 4. LLUVIA DE CONFETI MULTICOLOR FLOTANTE
                val confettiCount = 55
                for (c in 0 until confettiCount) {
                    val seed = (c * 17) % 100
                    val speed = 0.65f + ((c * 31) % 40) / 40f * 0.7f
                    val startX = ((c * 43) % 100) / 100f * size.width
                    val wobbleFreq = 3f + (c % 4) * 1.5f
                    val wobbleAmp = 22f + (c % 5) * 8f

                    val currentY = (progress * size.height * 1.25f * speed) - 30f + (c % 6 * 15f)
                    val currentX = startX + (kotlin.math.sin((progress * wobbleFreq + c).toDouble()) * wobbleAmp).toFloat()
                    val rotAngle = (progress * 360f * (if (c % 2 == 0) 1.5f else -1.5f) + c * 20f)

                    val confettiColor = when (c % 6) {
                        0 -> Color(0xFFFFD166) // Oro
                        1 -> Color(0xFF06D6A0) // Verde Fiesta
                        2 -> Color(0xFFEF476F) // Rojo/Rosa
                        3 -> Color(0xFF48CAE4) // Celeste
                        4 -> Color(0xFFF4A261) // Cobre
                        else -> Color(0xFFFFFFFF) // Blanco puro
                    }.copy(alpha = fiestaAlpha * 0.92f)

                    if (currentY in -20f..size.height + 20f && currentX in -20f..size.width + 20f) {
                        val pieceWidth = 9f + (c % 3) * 3f
                        val pieceHeight = 14f + (c % 4) * 4f
                        val halfW = pieceWidth / 2f
                        val halfH = pieceHeight / 2f

                        val radAngle = Math.toRadians(rotAngle.toDouble())
                        val cosA = kotlin.math.cos(radAngle).toFloat()
                        val sinA = kotlin.math.sin(radAngle).toFloat()

                        val p1 = Offset(currentX + (-halfW * cosA - -halfH * sinA), currentY + (-halfW * sinA + -halfH * cosA))
                        val p2 = Offset(currentX + (halfW * cosA - -halfH * sinA), currentY + (halfW * sinA + -halfH * cosA))
                        val p3 = Offset(currentX + (halfW * cosA - halfH * sinA), currentY + (halfW * sinA + halfH * cosA))
                        val p4 = Offset(currentX + (-halfW * cosA - halfH * sinA), currentY + (-halfW * sinA + halfH * cosA))

                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(p1.x, p1.y)
                            lineTo(p2.x, p2.y)
                            lineTo(p3.x, p3.y)
                            lineTo(p4.x, p4.y)
                            close()
                        }
                        drawPath(path = path, color = confettiColor)
                    }
                }
            }

            // 5. EMOJIS FESTIVOS FLOTANTES (🤠, ⭐, 🐎, 🌵, 👑)
            val floatingBadges = listOf(
                Triple("🤠", 0.15f, 0.45f),
                Triple("⭐", 0.80f, 0.35f),
                Triple("🏆", 0.50f, 0.25f),
                Triple("🐎", 0.25f, 0.65f),
                Triple("🌵", 0.75f, 0.60f),
                Triple("🎉", 0.85f, 0.75f),
                Triple("✨", 0.12f, 0.78f)
            )

            floatingBadges.forEachIndexed { bIdx, (emoji, relX, relY) ->
                val bProgress = ((progress - bIdx * 0.08f) / 0.8f).coerceIn(0f, 1f)
                if (bProgress > 0f && bProgress < 1f) {
                    val bAlpha = if (bProgress < 0.2f) bProgress / 0.2f else (1f - bProgress) / 0.8f
                    val bScale = if (bProgress < 0.3f) 0.5f + (bProgress / 0.3f) * 0.7f else 1.2f - (bProgress - 0.3f) * 0.3f
                    val bOffsetY = -bProgress * 120f + kotlin.math.sin((bProgress * 8f + bIdx).toDouble()).toFloat() * 15f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = (relX * size.width) - 24.dp.toPx()
                                translationY = (relY * size.height) + bOffsetY
                                scaleX = bScale
                                scaleY = bScale
                                alpha = (bAlpha * fiestaAlpha).coerceIn(0f, 1f)
                            }
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun GameEndOverlayDialog(
    gameStatus: GameStatus,
    timeElapsed: Int,
    difficultyName: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    onViewBoard: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val isWin = gameStatus == GameStatus.WON

    val borderColor = if (isWin) {
        if (isDarkTheme) Color(0xFF81C784).copy(alpha = 0.55f) else Color(0xFF388E3C).copy(alpha = 0.6f)
    } else {
        if (isDarkTheme) Color(0xFFFF8A80).copy(alpha = 0.55f) else Color(0xFFE53935).copy(alpha = 0.6f)
    }

    Dialog(onDismissRequest = onViewBoard) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color.Black.copy(alpha = 0.40f), RoundedCornerShape(26.dp))
                .padding(bottom = 6.dp)
                .background(
                    if (isDarkTheme) Color(0xFF2C221E) else MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(26.dp)
                )
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(26.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoticon Emblem Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color.Black.copy(alpha = 0.22f), CircleShape)
                        .padding(bottom = 4.dp)
                        .background(
                            if (isWin) {
                                if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                            } else {
                                if (isDarkTheme) Color(0xFFC62828) else Color(0xFFE53935)
                            },
                            CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWin) "🏆" else "💥",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = if (isWin) "¡VICTORIA EN EL RANCHO!" else "¡BUM! VALIÓ GAVER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.35f),
                            offset = Offset(2f, 3f),
                            blurRadius = 4f
                        )
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isWin) {
                        if (isDarkTheme) Color(0xFFFFD166) else Color(0xFF2E7D32)
                    } else {
                        if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFC62828)
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = if (isWin)
                        "¡Rifado padresanto! Has limpiado todas las minas de $difficultyName sin un solo rasguño."
                    else
                        "Pisaste una dinamita en $difficultyName. ¡No te agüites y vuelve a intentarlo!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Record Plaque for Victory
                if (isWin) {
                    val mins = timeElapsed / 60
                    val secs = timeElapsed % 60
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(bottom = 3.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isDarkTheme) Color(0xFFFFD166).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⏱️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tiempo Récord: ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFFFFD166) else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3D Tactile "Ver tablero" button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_view_board")
                        .bounceClick(onClick = onViewBoard)
                        .background(Color.Black.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
                        .padding(bottom = 4.dp)
                        .background(
                            if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver tablero",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Menú Button (3D tactile)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_end_home")
                            .bounceClick(onClick = onBackToMenu)
                            .background(Color.Black.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Menú",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Primary Action Button (3D tactile)
                    val actionBtnBg = if (isWin) {
                        if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFF388E3C)
                    } else {
                        if (isDarkTheme) Color(0xFFE65100) else Color(0xFFF57C00)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .testTag("btn_end_retry")
                            .bounceClick(onClick = onPlayAgain)
                            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                actionBtnBg,
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWin) "Otra Partida" else "Reintentar",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
