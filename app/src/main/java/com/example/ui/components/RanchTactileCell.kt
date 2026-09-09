package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CellState
import com.example.data.model.RanchFlagIcon
import com.example.ui.theme.CellBaseDay
import com.example.ui.theme.CellBaseNight
import com.example.ui.theme.CellBevelHighlightDay
import com.example.ui.theme.CellBevelHighlightNight
import com.example.ui.theme.CellBevelShadowDay
import com.example.ui.theme.CellBevelShadowNight
import com.example.ui.theme.CellExcavatedBorderDay
import com.example.ui.theme.CellExcavatedBorderNight
import com.example.ui.theme.CellExcavatedDay
import com.example.ui.theme.CellExcavatedNight
import com.example.ui.theme.CellPressedDay
import com.example.ui.theme.CellPressedNight
import com.example.ui.theme.getNumberColor

/**
 * Celda táctil 3D para el campo minado de Buscaminas del Rancho.
 *
 * Características de Game Feel y UI/UX:
 * - Bisel 3D trapezoidal iluminado/sombreado con renderizado en Canvas de 120 FPS.
 * - Desplazamiento y compresión física de sombra al mantener presionado (Press-down).
 * - Aspecto de terreno excavado rehundido al descubrirse.
 * - Pop elástico al revelarse números mediante overshoot spring.
 * - Soporte de medición de presión y chording reactivo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RanchTactileCell(
    cell: CellState,
    cellDp: Dp,
    ranchFlagIcon: RanchFlagIcon,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onClick: (pressure: Float) -> Unit,
    onLongClick: () -> Unit,
    onChord: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fontSizeSp = when {
        cellDp >= 40.dp -> 18.sp
        cellDp >= 36.dp -> 16.sp
        cellDp >= 32.dp -> 14.sp
        else -> 12.sp
    }

    var touchPressure by remember { mutableFloatStateOf(0.5f) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animación física de presión: la celda se deprime 1.5dp hacia abajo
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && !cell.isRevealed) 1.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 40),
        label = "cell_press_y"
    )

    // Micro-animación elástica de pop al aparecer un número
    val numberScale = remember { Animatable(1f) }
    LaunchedEffect(cell.isRevealed) {
        if (cell.isRevealed && cell.adjacentMines > 0) {
            numberScale.snapTo(0.4f)
            numberScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    // Micro-animación de squash & stretch al colocar sombrero/bandera
    val flagScale = remember { Animatable(1f) }
    LaunchedEffect(cell.isFlagged) {
        if (cell.isFlagged) {
            flagScale.snapTo(0.5f)
            flagScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    val cornerRadius = 4.dp
    val cellShape = RoundedCornerShape(cornerRadius)

    // Paletas de color según estado
    val highlightColor = if (isDarkTheme) CellBevelHighlightNight else CellBevelHighlightDay
    val shadowColor = if (isDarkTheme) CellBevelShadowNight else CellBevelShadowDay
    val baseTileColor = when {
        cell.isExploded -> MaterialTheme.colorScheme.errorContainer
        isPressed -> if (isDarkTheme) CellPressedNight else CellPressedDay
        else -> if (isDarkTheme) CellBaseNight else CellBaseDay
    }

    val excavatedColor = if (isDarkTheme) CellExcavatedNight else CellExcavatedDay
    val excavatedInnerShadow = if (isDarkTheme) CellExcavatedBorderNight else CellExcavatedBorderDay
    val excavatedRimLight = Color.White.copy(alpha = if (isDarkTheme) 0.08f else 0.22f)

    Box(
        modifier = modifier
            .size(cellDp)
            .aspectRatio(1f)
            .graphicsLayer {
                translationY = pressOffsetY.toPx()
            }
            .clip(cellShape)
            .drawBehind {
                val w = size.width
                val h = size.height

                if (cell.isRevealed) {
                    // 1. ESTADO REVELADO (Terreno excavado hundido)
                    drawRect(color = excavatedColor)

                    val innerBorderWidth = 1.2.dp.toPx()
                    // Sombra interna superior e izquierda
                    drawLine(
                        color = excavatedInnerShadow,
                        start = Offset(0f, 0f),
                        end = Offset(w, 0f),
                        strokeWidth = innerBorderWidth
                    )
                    drawLine(
                        color = excavatedInnerShadow,
                        start = Offset(0f, 0f),
                        end = Offset(0f, h),
                        strokeWidth = innerBorderWidth
                    )
                    // Borde de luz inferior y derecho
                    drawLine(
                        color = excavatedRimLight,
                        start = Offset(0f, h),
                        end = Offset(w, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = excavatedRimLight,
                        start = Offset(w, 0f),
                        end = Offset(w, h),
                        strokeWidth = 1.dp.toPx()
                    )
                } else {
                    // 2. ESTADO CUBIERTO (Losa táctil 3D con bisel auténtico)
                    val bevel = if (isPressed) 1.5.dp.toPx() else 2.8.dp.toPx()

                    // Bisel superior (Iluminado)
                    val topPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w - bevel, bevel)
                        lineTo(bevel, bevel)
                        close()
                    }
                    drawPath(topPath, color = highlightColor)

                    // Bisel izquierdo (Iluminado suave)
                    val leftPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(bevel, bevel)
                        lineTo(bevel, h - bevel)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(leftPath, color = highlightColor.copy(alpha = 0.90f))

                    // Bisel derecho (Sombreado)
                    val rightPath = Path().apply {
                        moveTo(w, 0f)
                        lineTo(w, h)
                        lineTo(w - bevel, h - bevel)
                        lineTo(w - bevel, bevel)
                        close()
                    }
                    drawPath(rightPath, color = shadowColor.copy(alpha = 0.85f))

                    // Bisel inferior (Sombra profunda)
                    val bottomPath = Path().apply {
                        moveTo(0f, h)
                        lineTo(w, h)
                        lineTo(w - bevel, h - bevel)
                        lineTo(bevel, h - bevel)
                        close()
                    }
                    drawPath(bottomPath, color = shadowColor)

                    // Cara central de la tecla
                    drawRect(
                        color = baseTileColor,
                        topLeft = Offset(bevel, bevel),
                        size = Size(w - (2 * bevel), h - (2 * bevel))
                    )
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touchPressure = down.pressure.coerceIn(0.1f, 1.0f)
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Usamos nuestra propia física de bisel y presión
                onClick = {
                    if (cell.isRevealed) {
                        onChord()
                    } else {
                        onClick(touchPressure)
                    }
                },
                onLongClick = onLongClick
            )
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        if (cell.isRevealed) {
            if (cell.isMine) {
                Text(
                    text = if (cell.isExploded) "💥" else "💣",
                    fontSize = fontSizeSp
                )
            } else if (cell.adjacentMines > 0) {
                val color = getNumberColor(cell.adjacentMines)
                Text(
                    text = "${cell.adjacentMines}",
                    fontSize = fontSizeSp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    modifier = Modifier.graphicsLayer {
                        scaleX = numberScale.value
                        scaleY = numberScale.value
                    }
                )
            }
        } else {
            if (cell.isFlagged) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = flagScale.value
                        scaleY = flagScale.value
                    },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedRanchFlagEmoji(
                        emoji = ranchFlagIcon.emoji,
                        fontSize = fontSizeSp
                    )
                }
            }
        }
    }
}

