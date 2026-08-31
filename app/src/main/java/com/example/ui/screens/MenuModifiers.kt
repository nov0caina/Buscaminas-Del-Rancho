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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Aplica un barrido de destello dorado brillante continuo, ideal para elementos VIP y destacados.
 */
fun Modifier.shimmerGoldenSweep(
    durationMillis: Int = 2400
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "golden_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "golden_shimmer_translate"
    )

    val shimmerColors = listOf(
        Color(0xFFFFD700).copy(alpha = 0.0f),
        Color(0xFFFFF7C2).copy(alpha = 0.35f),
        Color(0xFFFFD700).copy(alpha = 0.0f)
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, 0f),
            end = Offset(translateAnim + 200f, size.height)
        )
        drawRect(brush = brush)
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


