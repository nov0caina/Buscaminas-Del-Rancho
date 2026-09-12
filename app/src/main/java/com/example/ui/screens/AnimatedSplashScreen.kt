package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.particles.MysticSmokeParticleSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash Screen Cinemático Animado para nov0caina Studio.
 * - Humo volumétrico procedural con gradientes radiales suaves (sin halos duros).
 * - Máscara suave para difuminar bordes rectangulares del logo.
 * - Tipografía Serif estilizada y discreta para 'nov0caina'.
 * - Integración con insignia de Google Play Games en paleta cian.
 */
@Composable
fun AnimatedSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val smokeProgress = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.94f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(12f) }
    val footerAlpha = remember { Animatable(0f) }
    val screenFadeOut = remember { Animatable(1f) }

    val smokeSystem = remember { MysticSmokeParticleSystem(capacity = 80) }

    LaunchedEffect(Unit) {
        // Fase 1: Despertar del humo y niebla volumétrica cian (0.0s - 3.6s)
        launch {
            smokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3600, easing = LinearEasing)
            )
        }

        // Fase 2: Revelación suave del rostro recortado orgánicamente (0.5s - 2.0s)
        delay(500)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
            )
        }

        // Fase 3: Aparición de tipografía serif discreta y Google Play Games (1.6s - 2.6s)
        delay(1100)
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }
        launch {
            footerAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }

        // Fase 4: Transición de salida fluida al menú (3.2s - 3.6s)
        delay(1600)
        screenFadeOut.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030508))
            .alpha(screenFadeOut.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Toque para omitir animación
                onSplashFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val radius = kotlin.math.min(canvasWidth, canvasHeight) * 0.52f

            // 1. Capa de Sistema de Partículas de Humo Volumétrico Cian y Oscuro
            Canvas(modifier = Modifier.fillMaxSize()) {
                smokeSystem.setupMysticSmoke(centerX, centerY - 25f, radius)
                smokeSystem.render(this, smokeProgress.value, canvasWidth, canvasHeight)
            }

            // 2. Contenedor Central: Logo Recortado con Máscara Radial Suave + Tipografía
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-15).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rostro con máscara de recorte suave para eliminar bordes rectangulares
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            // Máscara radial de viñeta suave que desvanece cualquier esquina recta
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black,
                                        Color.Black,
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension * 0.49f
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nov0caina_logo),
                        contentDescription = "nov0caina logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tipografía Serif Discreta y Estilizada
                Box(
                    modifier = Modifier
                        .offset(y = textOffsetY.value.dp)
                        .alpha(textAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "n  o  v  0  c  a  i  n  a" + 
                                "\n\n        S t u d i o",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            fontSize = 17.sp,
                            letterSpacing = 8.sp,
                            color = Color(0xFFDDFBFF).copy(alpha = 0.90f),
                            shadow = Shadow(
                                color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                                offset = Offset(0f, 0f),
                                blurRadius = 14f
                            )
                        )
                    )
                }
            }

            // 3. Footer: Logo Google Play Games en Cian Armónico
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
                    .alpha(footerAlpha.value * 0.82f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_play_games),
                    contentDescription = "Google Play Games",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google Play Games",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                        color = Color(0xFF00E5FF).copy(alpha = 0.85f)
                    )
                )
            }
        }
    }
}
