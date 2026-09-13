package com.example.ui.screens
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
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

data class SinaloaBannerLocation(
    val drawableResId: Int,
    val city: String,
    val placeName: String
)

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

    val dayBanners = remember {
        listOf(
            SinaloaBannerLocation(R.drawable.banner_day_mazatlan, "Mazatlán", "Malecón y Tres Islas"),
            SinaloaBannerLocation(R.drawable.banner_day_guasave, "Guasave", "Playa Las Glorias"),
            SinaloaBannerLocation(R.drawable.banner_day_culiacan, "Culiacán", "Parque Las Riberas"),
            SinaloaBannerLocation(R.drawable.banner_day_los_mochis, "Los Mochis", "Bahía de Topolobampo")
        )
    }

    val nightBanners = remember {
        listOf(
            SinaloaBannerLocation(R.drawable.banner_night_mazatlan, "Mazatlán", "Olas Altas de Noche"),
            SinaloaBannerLocation(R.drawable.banner_night_guasave, "Guasave", "Malecón del Río Sinaloa"),
            SinaloaBannerLocation(R.drawable.banner_night_culiacan, "Culiacán", "Mirador de La Lomita"),
            SinaloaBannerLocation(R.drawable.banner_night_los_mochis, "Los Mochis", "Cerro de la Memoria y Faro")
        )
    }

    val currentBannerPack = if (isDarkTheme) nightBanners else dayBanners
    var bannerIndex by remember { mutableIntStateOf(0) }
    val safeBannerIndex = (bannerIndex % currentBannerPack.size).coerceAtLeast(0)
    val currentBanner = currentBannerPack[safeBannerIndex]

    // Rotación suave de paisajes sinaloenses cada 6.5 segundos
    LaunchedEffect(isDarkTheme) {
        while (true) {
            delay(6500L)
            bannerIndex = (bannerIndex + 1) % currentBannerPack.size
        }
    }
    
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
                        // 1. Crossfade between banner illustrations with Bidirectional Alpha Mask (DstIn)
                        Crossfade(
                            targetState = safeBannerIndex,
                            animationSpec = tween(700),
                            label = "banner_crossfade",
                            modifier = Modifier.fillMaxSize()
                        ) { index ->
                            val item = currentBannerPack.getOrNull(index) ?: currentBannerPack[0]
                            Image(
                                painter = painterResource(id = item.drawableResId),
                                contentDescription = "${item.placeName}, ${item.city}",
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
                        }

                        // 2. Aesthetic Location Badge & Indicator (Clickable to advance to next Sinaloa location)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 14.dp)
                                .bounceClick(
                                    onClick = {
                                        bannerIndex = (bannerIndex + 1) % currentBannerPack.size
                                    },
                                    bounceScale = 0.94f
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        if (isDarkTheme) {
                                            listOf(
                                                Color(0xE6261A12),
                                                Color(0xF0160F08)
                                            )
                                        } else {
                                            listOf(
                                                Color(0xEB4A2C18),
                                                Color(0xF5331C0E)
                                            )
                                        }
                                    )
                                )
                                .leatherStitchBorder(
                                    color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch else LeatherStitchDefaults.GoldStitch,
                                    strokeWidth = 1.2.dp,
                                    dashLength = 3.5.dp,
                                    gapLength = 3.dp,
                                    cornerRadius = 20.dp,
                                    inset = 2.dp
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📍",
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    AnimatedContent(
                                        targetState = "${currentBanner.city} • ${currentBanner.placeName}",
                                        transitionSpec = {
                                            (fadeIn(tween(350)) + slideInVertically { it / 2 })
                                                .togetherWith(fadeOut(tween(250)) + slideOutVertically { -it / 2 })
                                        },
                                        label = "location_text"
                                    ) { locationText ->
                                        Text(
                                            text = locationText,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp,
                                                shadow = androidx.compose.ui.graphics.Shadow(
                                                    color = Color.Black.copy(alpha = 0.85f),
                                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                    blurRadius = 3f
                                                )
                                            ),
                                            color = Color(0xFFFFF6E5),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                // Dots indicator (4 iconic locations)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    currentBannerPack.indices.forEach { dotIdx ->
                                        val isSelected = dotIdx == safeBannerIndex
                                        Box(
                                            modifier = Modifier
                                                .size(width = if (isSelected) 10.dp else 4.dp, height = 3.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (isSelected) {
                                                        if (isDarkTheme) Color(0xFFFFD166) else Color(0xFFFFE082)
                                                    } else {
                                                        Color.White.copy(alpha = 0.35f)
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }

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
                                    text = "BUSCAMINAS DEL RANCHO\n🤠",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = if (isDarkTheme) Color.Black.copy(alpha = 0.9f) else Color(0xFF3E1F07).copy(alpha = 0.95f),
                                            offset = androidx.compose.ui.geometry.Offset(2f, 4f),
                                            blurRadius = if (isDarkTheme) 8f else 10f
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
                                    .height(72.dp)
                                    .testTag("btn_resume_game")
                                    .bounceClick(onClick = onResumeGame)
                                    .background(color = Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                                    .padding(bottom = 5.dp)
                                    .background(
                                        color = if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFF388E3C),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .leatherStitchBorder(
                                        color = LeatherStitchDefaults.GreenStitch,
                                        cornerRadius = 16.dp
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
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
                                                text = "Sigue con tu partida pendiente",
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
                                isDarkTheme = isDarkTheme,
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
                                    .leatherStitchBorder(
                                        color = LeatherStitchDefaults.GoldStitch,
                                        cornerRadius = 18.dp
                                    )
                                    .shimmerGoldenSweep()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .widthIn(min = 76.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = patronPrice,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF2A1708),
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Clip
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
                            onClick = onLeaderboardClick,
                            isDarkTheme = isDarkTheme
                        )

                        // Achievements Button
                        MenuSecondaryButton(
                            title = "Logros del Rancho",
                            subtitle = "Tus Medallas y Trofeos",
                            icon = Icons.Default.Star,
                            testTag = "btn_achievements",
                            modifier = Modifier.staggeredEntrance(index = baseSecondaryIndex + 3),
                            onClick = onAchievementsClick,
                            isDarkTheme = isDarkTheme
                        )

                        // Settings Button
                        MenuSecondaryButton(
                            title = "Configuración",
                            subtitle = "Modo Noche, Sonido & Recordatorio",
                            icon = Icons.Default.Settings,
                            testTag = "btn_settings",
                            modifier = Modifier.staggeredEntrance(index = baseSecondaryIndex + 4),
                            onClick = onSettingsClick,
                            isDarkTheme = isDarkTheme
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
                            containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
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
                                text = "🌵 100% Offline • Sin Anuncios Intrusivos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
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
    isDarkTheme: Boolean = true,
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

    val stitchColor = when {
        isLocked -> LeatherStitchDefaults.GoldStitch
        isDarkTheme -> LeatherStitchDefaults.DarkThemeStitch
        else -> LeatherStitchDefaults.DayThemeStitch
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .testTag("btn_level_${difficulty.name.lowercase()}")
            .bounceClick(onClick = onClick)
            .background(color = Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(color = containerColor, shape = RoundedCornerShape(16.dp))
            .then(
                if (isLocked) Modifier.border(
                    1.5.dp,
                    Color(0xFFFFD700).copy(alpha = 0.6f),
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .leatherStitchBorder(
                color = stitchColor,
                cornerRadius = 16.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = difficulty.iconEmoji,
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = difficulty.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFFD700).copy(alpha = 0.25f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color(0xFFFFD700),
                                        RoundedCornerShape(6.dp)
                                    )
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLocked) "Desbloquea con Pase del Patrón" else difficulty.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        ),
                        color = contentColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .testTag(testTag)
            .bounceClick(onClick = onClick)
            .background(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.3f else 0.45f),
                shape = RoundedCornerShape(16.dp)
            )
            .leatherStitchBorder(
                color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.55f)
                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.75f),
                cornerRadius = 16.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
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
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
