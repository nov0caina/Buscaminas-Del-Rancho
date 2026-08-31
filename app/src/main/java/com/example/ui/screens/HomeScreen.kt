package com.example.ui.screens
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.math.PI
import com.example.ui.theme.RanchoBannerTitleDay
import com.example.ui.theme.RanchoBannerTitleNight
import com.example.R
import com.example.data.model.GameDifficulty
import com.example.ui.viewmodel.GameUiState
import androidx.compose.foundation.Canvas
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.RanchoSmokeParticleSystem

@Composable
fun HomeScreen(
    uiState: GameUiState,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isPatronUnlocked: Boolean = false,
    patronPrice: String = "$25.00 MXN",
    onResumeGame: () -> Unit,
    onSelectDifficulty: (difficulty: GameDifficulty, customRows: Int, customCols: Int, customMines: Int) -> Unit,
    onLeaderboardClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenPatronPassDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    
    val dustParticleSystem = remember { DustParticleSystem(150) }
    val smokeParticleSystem = remember { RanchoSmokeParticleSystem(150) }

    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_particles"
    )

    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic Particle Background
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Parallax effect: el fondo se mueve a la mitad de velocidad que el scroll
                        translationY = -scrollState.value * 0.35f
                        // Movimiento dinámico horizontal (sway) basado en el tiempo
                        translationX = (sin(progress * 2 * PI) * 30f).toFloat()
                        // Ligero efecto de escala para que se sienta que "respira"
                        val scale = 1f + (sin(progress * PI) * 0.05f).toFloat()
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                if (isDarkTheme) {
                    smokeParticleSystem.setupRanchoSmoke(size.width / 2f, size.height / 2f, size.width * 0.45f)
                    smokeParticleSystem.render(this, progress, size.width, size.height)
                } else {
                    dustParticleSystem.setupAmbientDust(size.width, size.height)
                    dustParticleSystem.render(this, progress)
                }
            }
            
            // Main Content wrapped with Safe Zones and width constraints
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hero Banner Header with bidirectional seamless Alpha DstIn mask
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                    ) {
                        // 1. Banner Image with Bidirectional Alpha Mask (DstIn) that fades out at top and bottom
                        Image(
                            painter = painterResource(id = R.drawable.img_rancho_banner),
                            contentDescription = "Banner del Rancho Sinaloense",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            0.0f to Color.Transparent,
                                            0.05f to Color.Black.copy(alpha = 0.25f),
                                            0.12f to Color.Black.copy(alpha = 0.70f),
                                            0.20f to Color.Black,
                                            0.40f to Color.Black,
                                            0.55f to Color.Black.copy(alpha = 0.80f),
                                            0.70f to Color.Black.copy(alpha = 0.35f),
                                            0.85f to Color.Transparent,
                                            1.0f to Color.Transparent
                                        ),
                                        blendMode = BlendMode.DstIn
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )

                        // 3. Title and Slogans
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .staggeredEntrance(index = 0),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "BUSCAMINAS DEL RANCHO ",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.9f),
                                            offset = androidx.compose.ui.geometry.Offset(2f, 4f),
                                            blurRadius = 8f
                                        )
                                    ),
                                    color = if (isDarkTheme) RanchoBannerTitleNight else RanchoBannerTitleDay,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Box(modifier = Modifier.idleFloat()) {
                                    Text(text = "🤠", fontSize = 28.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "PURO SINALOA VIEJON",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                color = if (isDarkTheme) Color(0xFFFFD166) else MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Limpia el terreno compa • Estilo Sinaloa",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.8f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Menu Action Cards
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Resume Game Button (3D Block Game Style)
                        AnimatedVisibility(visible = uiState.hasSavedGame) {
                            Box(
                                modifier = Modifier
                                    .staggeredEntrance(index = 1)
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .testTag("btn_resume_game")
                                    .bounceClick(onClick = onResumeGame)
                                    .background(color = Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                                    .padding(bottom = 5.dp)
                                    .background(
                                        color = if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFF388E3C),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                                                .padding(bottom = 3.dp)
                                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "Reanudar Partida 🤠",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Continúa tu juego guardado",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Seguir",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "⛏️ SELECCIONA TU NIVEL:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .staggeredEntrance(index = if (uiState.hasSavedGame) 2 else 1)
                                .padding(top = 4.dp, bottom = 2.dp)
                        )

                        // Direct Level Buttons
                        val baseLevelIndex = if (uiState.hasSavedGame) 3 else 2
                        GameDifficulty.entries.forEachIndexed { idx, diff ->
                            val isLocked = !isPatronUnlocked && (diff == GameDifficulty.EXPERTO || diff == GameDifficulty.PERSONALIZADA)
                            LevelDirectCard(
                                difficulty = diff,
                                isLocked = isLocked,
                                modifier = Modifier.staggeredEntrance(index = baseLevelIndex + idx),
                                onClick = {
                                    if (isLocked) {
                                        onOpenPatronPassDialog()
                                    } else if (diff == GameDifficulty.PERSONALIZADA) {
                                        showCustomDialog = true
                                    } else {
                                        onSelectDifficulty(diff, diff.rows, diff.cols, diff.mines)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val baseSecondaryIndex = baseLevelIndex + GameDifficulty.entries.size

                        // Patron Pass Banner / VIP Card
                        if (!isPatronUnlocked) {
                            Box(
                                modifier = Modifier
                                    .staggeredEntrance(index = baseSecondaryIndex)
                                    .fillMaxWidth()
                                    .bounceClick(onClick = onOpenPatronPassDialog)
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                    .padding(bottom = 5.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF4A3515), Color(0xFF2C1E0F))),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFC59B27))),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .shimmerGoldenSweep()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(Color(0xFFFFD700).copy(alpha = 0.2f), CircleShape)
                                                .border(1.dp, Color(0xFFFFD700), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "👑", fontSize = 22.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Pase del Patrón VIP",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFFD700)
                                            )
                                            Text(
                                                text = "99 Minas, Modo Libre & Distintivos",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = patronPrice,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF2A1708)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = "🏆 MÁS OPCIONES:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .staggeredEntrance(index = baseSecondaryIndex + 1)
                                .padding(top = 4.dp, bottom = 2.dp)
                        )

                        // Leaderboards Button
                        MenuSecondaryButton(
                            title = "Tabla de Posiciones",
                            subtitle = "Ranking Global & Sinaloa",
                            icon = Icons.Default.EmojiEvents,
                            testTag = "btn_leaderboards",
                            modifier = Modifier.staggeredEntrance(index = baseSecondaryIndex + 2),
                            onClick = onLeaderboardClick
                        )

                        // Achievements Button
                        MenuSecondaryButton(
                            title = "Logros del Rancho",
                            subtitle = "Medallas de Compadre",
                            icon = Icons.Default.Star,
                            testTag = "btn_achievements",
                            modifier = Modifier.staggeredEntrance(index = baseSecondaryIndex + 3),
                            onClick = onAchievementsClick
                        )

                        // Settings Button
                        MenuSecondaryButton(
                            title = "Configuración",
                            subtitle = "Modo Noche, Sonido & Recordatorio",
                            icon = Icons.Default.Settings,
                            testTag = "btn_settings",
                            modifier = Modifier.staggeredEntrance(index = baseSecondaryIndex + 4),
                            onClick = onSettingsClick
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (showCustomDialog) {
                        CustomDifficultyDialog(
                            isDarkTheme = isDarkTheme,
                            onDismiss = { showCustomDialog = false },
                            onConfirmCustom = { r, c, m ->
                                showCustomDialog = false
                                onSelectDifficulty(GameDifficulty.PERSONALIZADA, r, c, m)
                            }
                        )
                    }

                    // Footer Badge
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🌵 100% Offline • Sin Anuncios Intrusivos ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelDirectCard(
    difficulty: GameDifficulty,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (difficulty) {
        GameDifficulty.PRINCIPIANTE -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        GameDifficulty.INTERMEDIO -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        GameDifficulty.EXPERTO -> Pair(
            if (isLocked) Color(0xFF382515) else MaterialTheme.colorScheme.tertiaryContainer,
            if (isLocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onTertiaryContainer
        )
        GameDifficulty.PERSONALIZADA -> Pair(
            if (isLocked) Color(0xFF2E2018) else MaterialTheme.colorScheme.surfaceVariant,
            if (isLocked) Color(0xFFFFE082) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .testTag("btn_level_${difficulty.name.lowercase()}")
            .bounceClick(onClick = onClick)
            .background(color = Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(color = containerColor, shape = RoundedCornerShape(16.dp))
            .then(
                if (isLocked) Modifier.border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = difficulty.iconEmoji,
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = difficulty.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        if (isLocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFD700).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "🔒 VIP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isLocked) "Desbloquea con Pase del Patrón" else difficulty.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isLocked) "Pase" else "Jugar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (isLocked) {
                    Text(text = "👑", fontSize = 16.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuSecondaryButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .testTag(testTag)
            .bounceClick(onClick = onClick)
            .background(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
