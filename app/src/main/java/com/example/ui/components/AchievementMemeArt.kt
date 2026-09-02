package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Componente modular de arte cómico animado para logros del rancho.
 * Diseñado en capas procedurales en Jetpack Compose para facilitar la sustitución futura
 * por vectores o drawables personalizados sin alterar el resto de la interfaz.
 */
@Composable
fun AchievementMemeArt(
    achievementId: String,
    isReplaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "achievement_art_loop")

    val pulseScale by animateFloatAsState(
        targetValue = if (isReplaying) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "art_replay_pulse"
    )

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "art_idle_time"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        when (achievementId) {
            "first_win" -> FirstWinMemeArt(time)
            "patron_experto" -> PatronExpertoMemeArt(time)
            "fast_hand" -> FastHandMemeArt(time)
            "cazador_iguanas" -> CazadorIguanasMemeArt(time)
            "minero_veterano" -> MineroVeteranoMemeArt(time)
            "sin_banderas" -> SinBanderasMemeArt(time)
            else -> DefaultAchievementArt(time)
        }
    }
}

@Composable
private fun FirstWinMemeArt(time: Float) {
    val bobbingY = (sin(time * 2 * PI).toFloat() * 8f).dp
    val hatRotation = sin(time * 2 * PI).toFloat() * 12f
    val spurRotation = time * 360f

    Box(contentAlignment = Alignment.Center) {
        // Sombrero y Vaquero
        Text(
            text = "🤠",
            fontSize = 76.sp,
            modifier = Modifier
                .offset(y = bobbingY)
                .graphicsLayer {
                    rotationZ = hatRotation
                }
        )

        // Botas con espuelas relucientes
        Text(
            text = "👢",
            fontSize = 34.sp,
            modifier = Modifier
                .offset(x = (-36).dp, y = (32).dp + (bobbingY * 0.5f))
                .graphicsLayer {
                    rotationZ = -15f + (sin(time * 2 * PI).toFloat() * 8f)
                }
        )

        Text(
            text = "✨",
            fontSize = 24.sp,
            modifier = Modifier
                .offset(x = 38.dp, y = (-26).dp)
                .graphicsLayer {
                    rotationZ = spurRotation
                    alpha = (0.6f + 0.4f * sin(time * 2 * PI).toFloat()).coerceIn(0f, 1f)
                }
        )
    }
}

@Composable
private fun PatronExpertoMemeArt(time: Float) {
    val crownY = (-44).dp + (sin(time * 2 * PI).toFloat() * 6f).dp
    val crownRot = sin(time * 2 * PI).toFloat() * 8f
    val galloScale = 1f + (sin(time * 2 * PI).toFloat() * 0.06f)

    Box(contentAlignment = Alignment.Center) {
        // Corona levitando
        Text(
            text = "👑",
            fontSize = 44.sp,
            modifier = Modifier
                .offset(y = crownY)
                .graphicsLayer {
                    rotationZ = crownRot
                }
        )

        // Gallo con gafas oscuras
        Text(
            text = "🐔",
            fontSize = 72.sp,
            modifier = Modifier
                .offset(y = 10.dp)
                .graphicsLayer {
                    scaleX = galloScale
                    scaleY = galloScale
                }
        )

        // Lentes oscuros superpuestos
        Text(
            text = "🕶️",
            fontSize = 32.sp,
            modifier = Modifier
                .offset(x = 2.dp, y = 4.dp)
        )

        // Destello de oro
        Text(
            text = "💰",
            fontSize = 26.sp,
            modifier = Modifier
                .offset(x = 42.dp, y = 28.dp)
                .graphicsLayer {
                    rotationZ = sin(time * 2 * PI).toFloat() * 15f
                }
        )
    }
}

@Composable
private fun FastHandMemeArt(time: Float) {
    val jitterX = (sin(time * 12 * PI).toFloat() * 4f).dp
    val dustOffset = (cos(time * 2 * PI).toFloat() * 12f).dp

    Box(contentAlignment = Alignment.Center) {
        // Nubecitas de polvo de velocidad
        Text(
            text = "💨",
            fontSize = 38.sp,
            modifier = Modifier
                .offset(x = (-42).dp + dustOffset, y = 24.dp)
                .graphicsLayer {
                    alpha = (0.5f + 0.5f * sin(time * 4 * PI).toFloat()).coerceIn(0f, 1f)
                }
        )

        // Vaquero en carrera veloz
        Text(
            text = "🏃‍♂️",
            fontSize = 74.sp,
            modifier = Modifier
                .offset(x = jitterX, y = 0.dp)
                .graphicsLayer {
                    rotationZ = -14f
                }
        )

        // Relámpago de energía
        Text(
            text = "⚡",
            fontSize = 36.sp,
            modifier = Modifier
                .offset(x = 36.dp, y = (-32).dp)
                .graphicsLayer {
                    rotationZ = sin(time * 6 * PI).toFloat() * 20f
                    scaleX = 1f + 0.2f * sin(time * 8 * PI).toFloat()
                    scaleY = 1f + 0.2f * sin(time * 8 * PI).toFloat()
                }
        )
    }
}

@Composable
private fun CazadorIguanasMemeArt(time: Float) {
    val branchSway = sin(time * 2 * PI).toFloat() * 6f
    val tongueProgress = sin(time * 2 * PI).toFloat().coerceIn(0f, 1f)

    Box(contentAlignment = Alignment.Center) {
        // Rama de guamúchil
        Text(
            text = "🌿",
            fontSize = 46.sp,
            modifier = Modifier
                .offset(x = (-30).dp, y = 30.dp)
                .graphicsLayer {
                    rotationZ = branchSway
                }
        )

        // Iguana sinaloense
        Text(
            text = "🦎",
            fontSize = 72.sp,
            modifier = Modifier
                .offset(y = 2.dp)
                .graphicsLayer {
                    rotationZ = sin(time * 2 * PI).toFloat() * 8f
                }
        )

        // Resortera o diana de puntería
        Text(
            text = "🎯",
            fontSize = 30.sp,
            modifier = Modifier
                .offset(x = 38.dp, y = (-26).dp + (tongueProgress * 8f).dp)
                .graphicsLayer {
                    rotationZ = time * 360f
                }
        )
    }
}

@Composable
private fun MineroVeteranoMemeArt(time: Float) {
    val pickaxeAngle = sin(time * 4 * PI).toFloat() * 28f
    val sparkY = (sin(time * 4 * PI).toFloat() * -10f).dp

    Box(contentAlignment = Alignment.Center) {
        // Roca de la mina
        Text(
            text = "🪨",
            fontSize = 58.sp,
            modifier = Modifier.offset(x = 24.dp, y = 20.dp)
        )

        // Pico minero
        Text(
            text = "⛏️",
            fontSize = 70.sp,
            modifier = Modifier
                .offset(x = (-16).dp, y = (-10).dp)
                .graphicsLayer {
                    rotationZ = pickaxeAngle
                }
        )

        // Chispas de oro y diamantes
        Text(
            text = "💎",
            fontSize = 28.sp,
            modifier = Modifier
                .offset(x = 36.dp, y = (-24).dp + sparkY)
                .graphicsLayer {
                    alpha = sin(time * 4 * PI).toFloat().coerceIn(0.2f, 1f)
                    rotationZ = time * 180f
                }
        )
    }
}

@Composable
private fun SinBanderasMemeArt(time: Float) {
    val eyeLookX = (sin(time * 2 * PI).toFloat() * 10f).dp
    val magnifyingZoom = 1f + (sin(time * 2 * PI).toFloat() * 0.12f)

    Box(contentAlignment = Alignment.Center) {
        // Ojo perspicaz / colmillo
        Text(
            text = "👁️",
            fontSize = 68.sp,
            modifier = Modifier
                .offset(x = eyeLookX, y = 4.dp)
        )

        // Lupa de rastreo
        Text(
            text = "🔍",
            fontSize = 62.sp,
            modifier = Modifier
                .offset(x = 18.dp, y = (-12).dp)
                .graphicsLayer {
                    scaleX = magnifyingZoom
                    scaleY = magnifyingZoom
                    rotationZ = sin(time * 2 * PI).toFloat() * 14f
                }
        )

        // Destello de sospecha / foco
        Text(
            text = "💡",
            fontSize = 28.sp,
            modifier = Modifier
                .offset(x = (-36).dp, y = (-30).dp)
                .graphicsLayer {
                    alpha = (0.5f + 0.5f * sin(time * 3 * PI).toFloat()).coerceIn(0f, 1f)
                }
        )
    }
}

@Composable
private fun DefaultAchievementArt(time: Float) {
    val bobbingY = (sin(time * 2 * PI).toFloat() * 6f).dp
    Text(
        text = "🏆",
        fontSize = 72.sp,
        modifier = Modifier.offset(y = bobbingY)
    )
}
