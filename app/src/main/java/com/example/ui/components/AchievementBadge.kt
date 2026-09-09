package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

import com.example.ui.screens.shimmerGoldenSweep

/**
 * Insignia visual unificada para logros del rancho.
 * Renderiza ilustraciones de alta fidelidad con efectos de desbloqueo,
 * resplandor dorado o silueta desaturada con candado rústico cuando está bloqueado.
 */
@Composable
fun AchievementBadge(
    achievementId: String,
    isUnlocked: Boolean,
    size: Dp = 56.dp,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val drawableRes = RanchAchievementLore.getAchievementDrawable(achievementId)
    val bottomBevel = if (size >= 80.dp) 6.dp else if (size >= 50.dp) 4.dp else 2.dp
    val borderWidth = if (isHighlighted) 2.5.dp else if (isUnlocked) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .size(size)
            .background(
                Color.Black.copy(alpha = if (isUnlocked) 0.35f else 0.25f),
                CircleShape
            )
            .padding(bottom = bottomBevel)
            .clip(CircleShape)
            .background(
                if (isHighlighted) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF9C4),
                            Color(0xFFFFD54F),
                            Color(0xFFFF8F00)
                        )
                    )
                } else if (isUnlocked) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFECB3),
                            Color(0xFFFFB300),
                            Color(0xFF4E342E)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3E2723),
                            Color(0xFF1E130E)
                        )
                    )
                }
            )
            .border(
                width = borderWidth,
                brush = if (isHighlighted) {
                    Brush.linearGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFFFFD700), Color(0xFFFFA000))
                    )
                } else if (isUnlocked) {
                    Brush.linearGradient(
                        listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFF8D6E63))
                    )
                } else {
                    Brush.linearGradient(
                        listOf(Color(0xFF5D4037), Color(0xFF3E2723))
                    )
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ilustración del logro
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isUnlocked) 1.5.dp else 2.dp)
                .clip(CircleShape)
                .graphicsLayer {
                    if (!isUnlocked) {
                        alpha = 0.40f
                    }
                },
            colorFilter = if (!isUnlocked) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.15f) })
            } else null
        )

        // Barrido continuo y sutil de brillo especular sobre el icono desbloqueado
        if (isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isUnlocked) 1.5.dp else 2.dp)
                    .clip(CircleShape)
                    .shimmerGoldenSweep(
                        durationMillis = if (isHighlighted) 2200 else 3200,
                        shimmerColor = if (isHighlighted) Color(0xFFFFF9C4) else Color.White,
                        maxAlpha = if (isHighlighted) 0.38f else 0.22f
                    )
            )
        }

        // Overlay de candado si está bloqueado
        if (!isUnlocked) {
            val lockSize = (size * 0.46f).coerceAtLeast(18.dp)
            Box(
                modifier = Modifier
                    .size(lockSize)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_achievement_locked_padlock),
                    contentDescription = "Logro Bloqueado",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

