package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GameScoreEntity
import com.example.data.model.GameDifficulty
import com.example.data.repository.GlobalLeaderboardEntry
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.RanchoSmokeParticleSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LeaderboardScreen(
    localScores: List<GameScoreEntity>,
    recentMatches: List<GameScoreEntity> = emptyList(),
    globalEntries: List<GlobalLeaderboardEntry> = emptyList(),
    isAuthenticatedPlayGames: Boolean = false,
    playerNamePlayGames: String? = null,
    onSignInPlayGames: () -> Unit = {},
    onOpenPlayGamesLeaderboards: () -> Unit = {},
    onFilterChange: (GameDifficulty, Boolean) -> List<GlobalLeaderboardEntry> = { _, _ -> globalEntries },
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    // Tab: 0 -> Global, 1 -> Mis Récords, 2 -> Partidas Recientes
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.PRINCIPIANTE) }
    // Metric: true -> Menor Tiempo, false -> Más Victorias
    var isTimeMetric by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val dustParticleSystem = remember { DustParticleSystem(150) }
    val smokeParticleSystem = remember { RanchoSmokeParticleSystem(150) }

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_particles_leaderboard"
    )

    val currentGlobalEntries = remember(selectedDifficulty, isTimeMetric, globalEntries) {
        onFilterChange(selectedDifficulty, isTimeMetric)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic Particle Background (Parallax)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scrollOffset = if (listState.layoutInfo.totalItemsCount > 0) listState.firstVisibleItemScrollOffset else 0
                    translationY = -scrollOffset * 0.35f
                    translationX = (sin(progress * 2 * PI) * 30f).toFloat()
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

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header con botón Volver y acceso directo a Google Play Games
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chunky Back Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .bounceClick(onClick = onBack)
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(bottom = 5.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Posiciones",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 4f),
                                blurRadius = 6f
                            )
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Google Play Games Shortcut Button
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .bounceClick(onClick = onOpenPlayGamesLeaderboards)
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(bottom = 4.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_play_games),
                            contentDescription = "Google Play Games",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Play Games",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Play Games Connection Status Badge
            if (isAuthenticatedPlayGames && playerNamePlayGames != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🟢", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Conectado como $playerNamePlayGames",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3D Tab Selector: [Global] [Mis Récords] [Recientes]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabButton(
                    text = "Global 🌎",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f).testTag("tab_global_leaderboard"),
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.width(6.dp))
                TabButton(
                    text = "Mis Récords 🏆",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1.1f).testTag("tab_local_leaderboard"),
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.width(6.dp))
                TabButton(
                    text = "Recientes 📜",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1.1f).testTag("tab_recent_leaderboard"),
                    isDarkTheme = isDarkTheme
                )
            }

            // Metric Selector & Difficulty Chips (Para Global y Mis Récords)
            if (selectedTab != 2) {
                // Metric Selector: [⚡ Menor Tiempo] vs [👑 Más Victorias]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    MetricChip(
                        text = "⚡ Menor Tiempo",
                        isSelected = isTimeMetric,
                        onClick = { isTimeMetric = true },
                        isDarkTheme = isDarkTheme
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricChip(
                        text = "👑 Más Victorias",
                        isSelected = !isTimeMetric,
                        onClick = { isTimeMetric = false },
                        isDarkTheme = isDarkTheme
                    )
                }

                // Difficulty Filters (3D Chips)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(GameDifficulty.entries) { diff ->
                        val isSelected = selectedDifficulty == diff
                        val lipSize by animateDpAsState(targetValue = if (isSelected) 0.dp else 4.dp, label = "diffLip")

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("chip_diff_${diff.name.lowercase()}")
                                .bounceClick(onClick = { selectedDifficulty = diff })
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Black.copy(alpha = 0.25f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(bottom = lipSize)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${diff.iconEmoji} ${diff.displayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // CONTENIDO DE LAS PESTAÑAS
            when (selectedTab) {
                0 -> {
                    // TAB GLOBAL
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(currentGlobalEntries) { _, entry ->
                            GlobalScoreCard(
                                entry = entry,
                                isTimeMetric = isTimeMetric,
                                isDarkTheme = isDarkTheme
                            )
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }

                1 -> {
                    // TAB MIS RÉCORDS
                    val filteredLocal = localScores.filter { it.difficultyName == selectedDifficulty.displayName && it.isWin }
                    if (filteredLocal.isEmpty()) {
                        EmptyLeaderboardState(
                            message = "Aún no tienes victorias en ${selectedDifficulty.displayName}. ¡Ponte el sombrero y juega!",
                            isDarkTheme = isDarkTheme
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(filteredLocal) { index, score ->
                                LocalScoreCard(
                                    rank = index + 1,
                                    score = score,
                                    isTimeMetric = isTimeMetric,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }

                2 -> {
                    // TAB PARTIDAS RECIENTES
                    if (recentMatches.isEmpty()) {
                        EmptyLeaderboardState(
                            message = "Aún no has jugado partidas. ¡Inicia una partida en el Rancho para registrar tu historial!",
                            isDarkTheme = isDarkTheme
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentMatches) { match ->
                                RecentMatchCard(
                                    match = match,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, isDarkTheme: Boolean) {
    val lipSize by animateDpAsState(targetValue = if (isSelected) 0.dp else 4.dp, label = "tabLip")
    Box(
        modifier = modifier
            .height(44.dp)
            .bounceClick(onClick = onClick)
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(bottom = lipSize)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val lipSize by animateDpAsState(targetValue = if (isSelected) 0.dp else 3.dp, label = "metricLip")
    Box(
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(bottom = lipSize)
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocalScoreCard(
    rank: Int,
    score: GameScoreEntity,
    isTimeMetric: Boolean,
    isDarkTheme: Boolean
) {
    val mins = score.timeInSeconds / 60
    val secs = score.timeInSeconds % 60
    val dateStr = remember(score.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(score.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankBadge(rank = rank)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Vaquero Local",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${score.difficultyName} • $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = String.format("%02d:%02d", mins, secs),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun GlobalScoreCard(
    entry: GlobalLeaderboardEntry,
    isTimeMetric: Boolean,
    isDarkTheme: Boolean
) {
    val mins = entry.timeSeconds / 60
    val secs = entry.timeSeconds % 60

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankBadge(rank = entry.rank)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = entry.avatarEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = entry.playerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = entry.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isTimeMetric) {
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.winCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFA000),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "🏆", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun RecentMatchCard(
    match: GameScoreEntity,
    isDarkTheme: Boolean
) {
    val mins = match.timeInSeconds / 60
    val secs = match.timeInSeconds % 60
    val dateStr = remember(match.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(match.timestamp))
    }

    val isWin = match.isWin
    val badgeBg = if (isWin) Color(0xFF2E7D32) else Color(0xFFC62828)
    val badgeText = if (isWin) "✓ VICTORIA 🤠" else "💥 DETONADA 💣"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(badgeBg.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                            .border(1.dp, badgeBg.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isWin) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.difficultyName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${match.rows}x${match.cols} (${match.mines} minas) • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = String.format("%02d:%02d", mins, secs),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val (bg, textCol) = when (rank) {
        1 -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        2 -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        3 -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(Color.Black.copy(alpha = 0.2f), CircleShape)
            .padding(bottom = 4.dp)
            .background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            fontWeight = FontWeight.ExtraBold,
            color = textCol,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun EmptyLeaderboardState(message: String, isDarkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
