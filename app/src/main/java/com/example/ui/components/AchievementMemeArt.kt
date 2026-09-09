package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import com.example.ui.screens.shimmerGoldenSweep

/**
 * Componente de arte heroico animado para el modal de celebración de logros.
 * Despliega la insignia ilustrada de alta fidelidad con microinteracciones de
 * respiración, balanceo, barrido de brillo dorado y rebote elástico (squash & stretch) al revivir festejo.
 */
@Composable
fun AchievementMemeArt(
    achievementId: String,
    isReplaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val drawableRes = RanchAchievementLore.getAchievementDrawable(achievementId)
    val infiniteTransition = rememberInfiniteTransition(label = "achievement_hero_loop")

    // Ambient floating time loop
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_idle_time"
    )

    // Squash & Stretch physics upon celebration replay
    val squashScaleX = remember { Animatable(1f) }
    val squashScaleY = remember { Animatable(1f) }

    LaunchedEffect(isReplaying) {
        if (isReplaying) {
            // Impact squash
            squashScaleX.animateTo(1.28f, tween(120, easing = FastOutSlowInEasing))
            squashScaleY.animateTo(0.78f, tween(120, easing = FastOutSlowInEasing))
            // Stretch overshoot
            squashScaleX.animateTo(
                0.88f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            squashScaleY.animateTo(
                1.22f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            // Settle to rest
            squashScaleX.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            )
            squashScaleY.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            )
        } else {
            squashScaleX.snapTo(1f)
            squashScaleY.snapTo(1f)
        }
    }

    // Ambient floating values
    val floatY = (sin(time * 2 * PI).toFloat() * 6f).dp
    val tiltRotation = sin(time * 2 * PI).toFloat() * 3.5f
    val breathScale = 1f + (sin(time * 2 * PI).toFloat() * 0.035f)

    Box(
        modifier = modifier
            .size(150.dp)
            .offset(y = floatY)
            .graphicsLayer {
                scaleX = breathScale * squashScaleX.value
                scaleY = breathScale * squashScaleY.value
                rotationZ = tiltRotation
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer Pulsing Halo
        Box(
            modifier = Modifier
                .size(148.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.40f + (sin(time * 2 * PI).toFloat() * 0.15f)),
                            Color(0xFFFFA000).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 3D Shadow Base
        Box(
            modifier = Modifier
                .size(136.dp)
                .background(Color.Black.copy(alpha = 0.40f), CircleShape)
                .padding(bottom = 7.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF9C4),
                            Color(0xFFFFD54F),
                            Color(0xFFE65100),
                            Color(0xFF3E2723)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFD700),
                            Color(0xFFFFA000),
                            Color(0xFFFFE082)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // High-Resolution Illustrated Badge
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(CircleShape)
            )

            // Specular Light Glint Sweep (Continuous seamless loop)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .shimmerGoldenSweep(
                        durationMillis = 2800,
                        shimmerColor = Color(0xFFFFF9C4),
                        maxAlpha = 0.45f
                    )
            )
        }
    }
}
