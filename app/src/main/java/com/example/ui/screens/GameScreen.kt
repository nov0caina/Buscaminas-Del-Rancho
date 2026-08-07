package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CellState
import com.example.data.model.ClickMode
import com.example.data.model.GameStatus
import com.example.data.model.VaqueroFace
import com.example.ui.theme.Number1Blue
import com.example.ui.theme.Number2Green
import com.example.ui.theme.Number3Red
import com.example.ui.theme.Number4Purple
import com.example.ui.theme.Number5Maroon
import com.example.ui.theme.Number6Teal
import com.example.ui.theme.Number7Black
import com.example.ui.theme.Number8Gray
import com.example.ui.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    onCellClick: (row: Int, col: Int) -> Unit,
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
    var showEndDialog by remember(uiState.gameStatus) { mutableStateOf(true) }

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
                        text = "⛏️ Clic simple: Revelar  •  🤠 Mantén presionado: Poner Sombrero",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
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
                    onFaceClick = onResetGame
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Minefield Grid container with free panning and zoom gestures
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    val cellDp = when {
                        uiState.cols <= 9 -> 36.dp
                        uiState.cols <= 12 -> 32.dp
                        else -> 28.dp
                    }

                    val gridWidth = (cellDp * uiState.cols) + (2.dp * (uiState.cols - 1))
                    val gridHeight = (cellDp * uiState.rows) + (2.dp * (uiState.rows - 1))
                    val boardWidth = gridWidth + 20.dp
                    val boardHeight = gridHeight + 20.dp

                    val availableWidth = (maxWidth - 16.dp).coerceAtLeast(100.dp)
                    val availableHeight = (maxHeight - 16.dp).coerceAtLeast(100.dp)

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
                        Box(
                            modifier = Modifier
                                .requiredSize(width = boardWidth, height = boardHeight)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                                .shadow(6.dp, RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            MinefieldGrid(
                                rows = uiState.rows,
                                cols = uiState.cols,
                                grid = uiState.grid,
                                onCellClick = onCellClick,
                                onCellLongClick = onCellLongClick,
                                onCellChord = onCellChord
                            )
                        }
                    }

                    // Floating Zoom and Centering Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SmallFloatingActionButton(
                            onClick = { scale = (scale * 1.25f).coerceAtMost(4.0f) },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.testTag("btn_zoom_in")
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        SmallFloatingActionButton(
                            onClick = { scale = (scale / 1.25f).coerceAtLeast(fitScale * 0.7f) },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.testTag("btn_zoom_out")
                        ) {
                            Text("−", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                scale = fitScale
                                offsetX = 0f
                                offsetY = 0f
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.testTag("btn_zoom_reset")
                        ) {
                            Text("🎯", fontSize = 16.sp)
                        }
                    }
                }
            }

            // End-of-game victory/loss overlay dialog or floating button
            if (uiState.gameStatus == GameStatus.WON || uiState.gameStatus == GameStatus.LOST) {
                if (showEndDialog) {
                    GameEndOverlayDialog(
                        gameStatus = uiState.gameStatus,
                        timeElapsed = uiState.timeElapsed,
                        difficultyName = uiState.difficulty.displayName,
                        onPlayAgain = onResetGame,
                        onBackToMenu = onBackToMenu,
                        onViewBoard = { showEndDialog = false }
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showEndDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ver Resultado", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onResetGame,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reintentar", fontWeight = FontWeight.Bold)
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
    onFaceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mines Counter Display
            HudCounterBox(
                emoji = "🧨",
                valueString = String.format("%03d", minesLeft),
                testTag = "hud_mines_counter"
            )

            // Vaquero Reset Face Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                testTag = "hud_timer"
            )
        }
    }
}

@Composable
private fun HudCounterBox(
    emoji: String,
    valueString: String,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
                color = MaterialTheme.colorScheme.primary,
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
    onCellClick: (row: Int, col: Int) -> Unit,
    onCellLongClick: (row: Int, col: Int) -> Unit,
    onCellChord: (row: Int, col: Int) -> Unit
) {
    val cellDp = when {
        cols <= 9 -> 36.dp
        cols <= 12 -> 32.dp
        else -> 28.dp
    }

    val gridWidth = (cellDp * cols) + (2.dp * (cols - 1))
    val gridHeight = (cellDp * rows) + (2.dp * (rows - 1))

    Column(
        modifier = Modifier.requiredSize(width = gridWidth, height = gridHeight),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (r in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (c in 0 until cols) {
                    val index = r * cols + c
                    if (index in grid.indices) {
                        CellItem(
                            cell = grid[index],
                            cellDp = cellDp,
                            onClick = { onCellClick(r, c) },
                            onLongClick = { onCellLongClick(r, c) },
                            onChord = { onCellChord(r, c) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CellItem(
    cell: CellState,
    cellDp: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onChord: () -> Unit
) {
    val fontSizeSp = when {
        cellDp >= 36.dp -> 16.sp
        cellDp >= 32.dp -> 14.sp
        else -> 12.sp
    }

    Box(
        modifier = Modifier
            .size(cellDp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    cell.isExploded -> MaterialTheme.colorScheme.errorContainer
                    cell.isRevealed -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            .border(
                width = if (cell.isRevealed) 0.5.dp else 1.5.dp,
                color = if (cell.isRevealed)
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
            .combinedClickable(
                onClick = {
                    if (cell.isRevealed) {
                        onChord()
                    } else {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            )
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isFlagged -> {
                Text(text = "🤠", fontSize = fontSizeSp)
            }
            cell.isRevealed -> {
                if (cell.isMine) {
                    Text(text = if (cell.isExploded) "💥" else "💣", fontSize = fontSizeSp)
                } else if (cell.adjacentMines > 0) {
                    val color = getNumberColor(cell.adjacentMines)
                    Text(
                        text = "${cell.adjacentMines}",
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun getNumberColor(number: Int): Color {
    return when (number) {
        1 -> Number1Blue
        2 -> Number2Green
        3 -> Number3Red
        4 -> Number4Purple
        5 -> Number5Maroon
        6 -> Number6Teal
        7 -> Number7Black
        8 -> Number8Gray
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun GameEndOverlayDialog(
    gameStatus: GameStatus,
    timeElapsed: Int,
    difficultyName: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    onViewBoard: () -> Unit
) {
    val isWin = gameStatus == GameStatus.WON

    Dialog(onDismissRequest = onViewBoard) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isWin) "🎉 ¡VICTORIA EN EL RANCHO! 🎉" else "💥 ¡BUM! VALIÓ GAVER 💥",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isWin) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isWin)
                        "¡Rifado padresanto! Has limpiado todas las minas de $difficultyName sin un solo rasguño."
                    else
                        "Pisaste una dinamita en $difficultyName. ¡No te agüites y vuelve a intentarlo!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isWin) {
                    val mins = timeElapsed / 60
                    val secs = timeElapsed % 60
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⏱️ Tiempo Record: ", fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Button to view the board state
                OutlinedButton(
                    onClick = onViewBoard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_view_board"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("👁️ Ver cómo quedó el tablero", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onBackToMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_end_home"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Menú")
                        }
                    }

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_end_retry"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWin) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isWin) "Otra Partida" else "Reintentar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
