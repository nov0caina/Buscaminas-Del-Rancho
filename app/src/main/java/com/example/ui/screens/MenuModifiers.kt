package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Modifier.bounceClick(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    bounceScale: Float = 0.88f,
    reboundDelayMillis: Long = 100L
): Modifier = composed {
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundManager = remember { com.example.audio.SoundManager.getInstance(context) }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val coroutineScope = rememberCoroutineScope()
    val animScale = remember { Animatable(1f) }
    var isHandlingClick by remember { mutableStateOf(false) }
    var touchPressure by remember { mutableStateOf(0.5f) }

    // Respond immediately to touch down / touch up
    LaunchedEffect(isPressed) {
        if (!isHandlingClick) {
            if (isPressed) {
                animScale.animateTo(
                    targetValue = bounceScale,
                    animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing)
                )
            } else {
                animScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    this
        .graphicsLayer {
            val focusMultiplier = if (isFocused) 1.03f else 1f
            scaleX = animScale.value * focusMultiplier
            scaleY = animScale.value * focusMultiplier
        }
        .then(
            if (isFocused) {
                Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                Modifier
            }
        )
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                touchPressure = down.pressure.coerceIn(0.1f, 1.0f)
            }
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                if (!isHandlingClick) {
                    isHandlingClick = true
                    soundManager.playButtonClick(touchPressure)
                    coroutineScope.launch {
                        // 1. Force the button to squash down noticeably even on 5ms taps
                        animScale.animateTo(
                            targetValue = bounceScale,
                            animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing)
                        )
                        // 2. Spring back with bouncy physics
                        launch {
                            animScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        // 3. Allow player to see the spring rebound before navigating
                        delay(reboundDelayMillis)
                        onClick()
                        // Reset lock after a safety margin
                        delay(200L)
                        isHandlingClick = false
                    }
                }
            }
        )
}

/**
 * Aplica una animación de entrada escalonada suave (fade in + slide up con resorte)
 * basada en el índice secuencial del elemento.
 */
fun Modifier.staggeredEntrance(
    index: Int,
    baseDelayMillis: Long = 45L,
    initialOffsetY: Float = 60f
): Modifier = composed {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(index * baseDelayMillis)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    this.graphicsLayer {
        alpha = animProgress.value.coerceIn(0f, 1f)
        translationY = (1f - animProgress.value) * initialOffsetY
    }
}

/**
 * Aplica un barrido de destello dorado brillante continuo y sin cortes,
 * con proyección normal cerrada sobre cualquier tamaño o proporción de elemento.
 */
fun Modifier.shimmerGoldenSweep(
    durationMillis: Int = 2600,
    shimmerColor: Color = Color(0xFFFFF7C2),
    maxAlpha: Float = 0.38f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "golden_shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "golden_shimmer_progress"
    )

    this.drawWithContent {
        drawContent()
        val width = size.width
        val height = size.height
        if (width > 0f && height > 0f) {
            val tiltX = height * 0.45f
            val len = kotlin.math.sqrt(height * height + tiltX * tiltX)
            val nx = height / len
            val ny = -tiltX / len

            // Proyecciones de las esquinas extremas del canvas sobre la normal
            val pMin = -(height * tiltX) / len
            val pMax = (width * height) / len

            val bandWidth = (width * 0.42f).coerceIn(40f, 260f)
            val buffer = bandWidth * 0.35f

            // Recorrido cerrado: en progress = 0 y progress = 1 todo el canvas tiene alpha = 0
            val startP = pMin - bandWidth - buffer
            val endP = pMax + buffer
            val currentP = startP + progress * (endP - startP)

            val startOffset = Offset(currentP * nx, currentP * ny)
            val endOffset = Offset((currentP + bandWidth) * nx, (currentP + bandWidth) * ny)

            val brush = Brush.linearGradient(
                colors = listOf(
                    shimmerColor.copy(alpha = 0f),
                    shimmerColor.copy(alpha = maxAlpha * 0.25f),
                    shimmerColor.copy(alpha = maxAlpha),
                    shimmerColor.copy(alpha = maxAlpha * 0.25f),
                    shimmerColor.copy(alpha = 0f)
                ),
                start = startOffset,
                end = endOffset
            )
            drawRect(brush = brush)
        }
    }
}

/**
 * Aplica una micro-animación de flotación y respiración sutil y continua.
 */
fun Modifier.idleFloat(
    durationMillis: Int = 2400,
    maxOffsetY: Float = 6f,
    scaleRange: Float = 0.04f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "idle_float")
    val floatAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_float_offset"
    )

    this.graphicsLayer {
        translationY = floatAnim * maxOffsetY
        scaleX = 1f + (floatAnim * scaleRange * 0.5f)
        scaleY = 1f + (floatAnim * scaleRange * 0.5f)
    }
}

/**
 * Valores y configuraciones predeterminadas para los bordes de talabartería (pespunte y remaches sobre cuero).
 */
object LeatherStitchDefaults {
    val StrokeWidth: Dp = 1.5.dp
    val DashLength: Dp = 4.5.dp
    val GapLength: Dp = 3.5.dp
    val Inset: Dp = 3.5.dp
    val CornerRadius: Dp = 16.dp

    // Paletas temáticas de alta legibilidad y artesanía de talabartería
    val GoldStitch: Color = Color(0xFFFFD700).copy(alpha = 0.85f)
    val AmberStitch: Color = Color(0xFFE2B85A).copy(alpha = 0.85f)
    val DarkThemeStitch: Color = Color(0xFFFFE082).copy(alpha = 0.65f)
    // Hilo de talabartería encerado oscuro para modo Día (máximo contraste y definición sobre cuero claro/beige)
    val DayThemeStitch: Color = Color(0xFF3E200C).copy(alpha = 0.80f)
    val DayThemePanelStitch: Color = Color(0xFF4A2A14).copy(alpha = 0.70f)
    val GreenStitch: Color = Color(0xFFA5D6A7).copy(alpha = 0.75f)
}

/**
 * Dibuja un borde perimetral discontinuo (*saddle stitch* / remaches y pespunte de talabartería)
 * con esquinas redondeadas concéntricas al contenedor, simulando costura artesanal sobre cuero.
 *
 * Emplea [drawWithCache] para garantizar que el cálculo del trazado ([Path]) y el efecto punteado
 * ([PathEffect]) se conserven en caché y solo se reevalúen si cambian las dimensiones del componente,
 * evitando cualquier asignación en el hilo de render durante animaciones a 60/120 FPS.
 */
fun Modifier.leatherStitchBorder(
    color: Color,
    strokeWidth: Dp = LeatherStitchDefaults.StrokeWidth,
    dashLength: Dp = LeatherStitchDefaults.DashLength,
    gapLength: Dp = LeatherStitchDefaults.GapLength,
    cornerRadius: Dp = LeatherStitchDefaults.CornerRadius,
    inset: Dp = LeatherStitchDefaults.Inset
): Modifier = this.drawWithCache {
    val strokeWidthPx = strokeWidth.toPx()
    val insetPx = inset.toPx() + strokeWidthPx / 2f
    val cornerRadiusPx = (cornerRadius.toPx() - insetPx).coerceAtLeast(0f)
    val width = size.width - insetPx * 2f
    val height = size.height - insetPx * 2f

    if (width <= 0f || height <= 0f) {
        onDrawWithContent {
            drawContent()
        }
    } else {
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(insetPx, insetPx),
                        size = Size(width, height)
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            )
        }
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
        val stroke = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect,
            cap = StrokeCap.Round
        )

        onDrawWithContent {
            drawContent()
            drawPath(
                path = path,
                color = color,
                style = stroke
            )
        }
    }
}


