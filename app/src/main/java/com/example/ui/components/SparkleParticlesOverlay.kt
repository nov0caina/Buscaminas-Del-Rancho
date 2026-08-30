package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class SparkleParticle(
    val originXRatio: Float,
    val originYRatio: Float,
    val angleRad: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val isStar: Boolean,
    val rotationSpeed: Float
)

@Composable
fun SparkleParticlesOverlay(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 26
) {
    if (triggerKey == null) return

    val progress = remember(triggerKey) { Animatable(0f) }

    val particles = remember(triggerKey) {
        val colors = listOf(
            Color(0xFFFFD166), // Gold Light
            Color(0xFFFFB703), // Gold Vibrant
            Color(0xFFFB8500), // Amber Gold
            Color(0xFFFFFFFF), // Diamond White Spark
            Color(0xFFF4A261), // Ranch Sand
            Color(0xFFE76F51)  // Coral Fiesta
        )
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 320f + 120f
            SparkleParticle(
                originXRatio = 0.5f + (Random.nextFloat() - 0.5f) * 0.4f,
                originYRatio = 0.08f,
                angleRad = angle,
                speed = speed,
                size = Random.nextFloat() * 7f + 4f,
                color = colors.random(),
                isStar = Random.nextBoolean(),
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
    }

    val t = progress.value
    if (t in 0.01f..0.99f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            particles.forEach { p ->
                val startX = canvasW * p.originXRatio
                val startY = canvasH * p.originYRatio

                // Physics: Velocity + Gravity + Drag
                val dist = p.speed * t * (1f - 0.35f * t)
                val gravityDrop = 480f * t * t
                val currentX = startX + cos(p.angleRad) * dist
                val currentY = startY + sin(p.angleRad) * dist + gravityDrop

                // Alpha fades out towards the end
                val alpha = ((1f - t) * 1.4f).coerceIn(0f, 1f)
                val currentColor = p.color.copy(alpha = alpha)

                if (p.isStar) {
                    drawSparkleStar(
                        center = Offset(currentX, currentY),
                        radius = p.size * (1f - 0.2f * t),
                        color = currentColor,
                        rotation = p.rotationSpeed * t
                    )
                } else {
                    drawCircle(
                        color = currentColor,
                        radius = p.size * (1f - 0.3f * t),
                        center = Offset(currentX, currentY)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSparkleStar(
    center: Offset,
    radius: Float,
    color: Color,
    rotation: Float
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - radius * 1.5f)
            quadraticTo(center.x, center.y, center.x + radius * 1.5f, center.y)
            quadraticTo(center.x, center.y, center.x, center.y + radius * 1.5f)
            quadraticTo(center.x, center.y, center.x - radius * 1.5f, center.y)
            quadraticTo(center.x, center.y, center.x, center.y - radius * 1.5f)
            close()
        }
        drawPath(path = path, color = color)
    }
}
