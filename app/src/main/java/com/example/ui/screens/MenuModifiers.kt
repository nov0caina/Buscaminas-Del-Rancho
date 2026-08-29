package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                if (!isHandlingClick) {
                    isHandlingClick = true
                    soundManager.playButtonClick()
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


