package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Renderiza el emoji del estandarte/marca con aceleración por GPU (graphicsLayer)
 * aplicando la animación por defecto "Sello de Rancho" (impacto firme con rebote elástico).
 */
@Composable
fun AnimatedRanchFlagEmoji(
    emoji: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val progress = animProgress.value
    // Sello de Rancho: cae con fuerza e impacto elástico desde arriba
    val scale = 1.9f - (0.9f * progress)
    val translationY = (1f - progress) * -20f
    val rotationZ = (1f - progress) * -12f
    val alpha = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier.graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.rotationZ = rotationZ
            this.translationY = translationY
            this.alpha = alpha
        },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = fontSize)
    }
}
