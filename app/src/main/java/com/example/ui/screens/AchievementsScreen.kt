package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AchievementEntity
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.RanchoSmokeParticleSystem
import kotlin.math.PI
import kotlin.math.sin

import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.R

import androidx.compose.runtime.rememberCoroutineScope
import com.example.audio.SoundManager
import com.example.ui.components.AchievementDetailModal
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    highlightedAchievementId: String? = null,
    onOpenPlayGamesAchievements: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedAchievement by remember { mutableStateOf<AchievementEntity?>(null) }
    var shakingAchievementId by remember { mutableStateOf<String?>(null) }

    // Auto-scroll to highlighted achievement if coming from notification or toast
    LaunchedEffect(highlightedAchievementId, achievements) {
        if (highlightedAchievementId != null && achievements.isNotEmpty()) {
            val index = achievements.indexOfFirst { it.id == highlightedAchievementId }
            if (index >= 0) {
                // Item 0 is progress card, items 1..N are achievements
                listState.animateScrollToItem(index + 1)
            }
        }
    }

    val dustParticleSystem = remember { DustParticleSystem(150) }
    val smokeParticleSystem = remember { RanchoSmokeParticleSystem(150) }

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransitionAchievements")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_particles_achievements"
    )

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
            
            // Custom Game Header
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

                    Column {
                        Text(
                            text = "Logros del Rancho 🏆",
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
                        Text(
                            text = "Tus hazañas en el campo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Google Play Games Achievements Overlay Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .bounceClick(onClick = onOpenPlayGamesAchievements)
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(bottom = 5.dp)
                        .background(Color(0xFF2E7D32), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Abrir logros en Google Play Games",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
            val totalCount = achievements.size

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Global Progress Header Card
                item {
                    AchievementProgressCard(
                        unlockedCount = unlockedCount,
                        totalCount = totalCount,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.staggeredEntrance(index = 0)
                    )
                }

                itemsIndexed(achievements) { index, achievement ->
                    val isHighlighted = achievement.id == highlightedAchievementId
                    val isShaking = achievement.id == shakingAchievementId
                    AchievementCard(
                        achievement = achievement,
                        isDarkTheme = isDarkTheme,
                        isHighlighted = isHighlighted,
                        isShaking = isShaking,
                        onClick = {
                            if (achievement.isUnlocked) {
                                selectedAchievement = achievement
                            } else {
                                scope.launch {
                                    shakingAchievementId = achievement.id
                                    soundManager.playLockedAchievementSound()
                                    delay(350L)
                                    if (shakingAchievementId == achievement.id) {
                                        shakingAchievementId = null
                                    }
                                }
                            }
                        },
                        modifier = Modifier.staggeredEntrance(index = index + 1)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // Interactive Achievement Celebration Modal Dialog
        selectedAchievement?.let { achievement ->
            AchievementDetailModal(
                achievement = achievement,
                onDismiss = { selectedAchievement = null },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun AchievementProgressCard(
    unlockedCount: Int,
    totalCount: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalCount > 0) (unlockedCount * 100 / totalCount) else 0
    val progressRatio = if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progressRatio else 0f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "global_achievement_progress"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 6.dp)
            .background(
                if (isDarkTheme) Color(0xFF2C221E) else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Progreso del Rancho",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "$unlockedCount de $totalCount ($percentage%)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkTheme) Color(0xFFFFD166) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Rounded Global Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0.001f, 1f))
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementEntity,
    isDarkTheme: Boolean,
    isHighlighted: Boolean = false,
    isShaking: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUnlocked = achievement.isUnlocked
    val surfaceCol = if (isHighlighted) {
        if (isDarkTheme) Color(0xFF4A3419) else Color(0xFFFFF8E1)
    } else if (isUnlocked) {
        if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.primaryContainer
    } else {
        if (isDarkTheme) Color(0xFF1E1714) else MaterialTheme.colorScheme.surface
    }

    val targetRatio = (achievement.progress.toFloat() / achievement.maxProgress).coerceIn(0f, 1f)
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedRatio by animateFloatAsState(
        targetValue = if (animationPlayed) targetRatio else 0f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "achievement_item_progress"
    )

    val shakeOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            val keyframes = listOf(0f, -10f, 10f, -8f, 8f, -4f, 4f, 0f)
            for (offset in keyframes) {
                shakeOffsetX.animateTo(
                    targetValue = offset,
                    animationSpec = tween(durationMillis = 40, easing = LinearEasing)
                )
            }
        } else {
            shakeOffsetX.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("achievement_card_${achievement.id}")
            .graphicsLayer {
                translationX = shakeOffsetX.value
            }
            .bounceClick(onClick = onClick)
            .then(
                if (isHighlighted) Modifier.shimmerGoldenSweep(durationMillis = 2000) else Modifier
            )
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 6.dp)
            .background(surfaceCol, RoundedCornerShape(16.dp))
            .border(
                width = if (isHighlighted) 2.dp else if (isUnlocked) 1.5.dp else 1.dp,
                color = if (isHighlighted) Color(0xFFFFD700) else if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (isHighlighted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .background(Color(0xFFFFD700).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "✨ ¡NUEVO LOGRO DESBLOQUEADO! 🤠",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkTheme) Color(0xFFFFD700) else Color(0xFFB78103)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                        .padding(bottom = 4.dp)
                        .background(
                            if (isHighlighted) Color(0xFFFFD700) else if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = achievement.iconEmoji,
                        fontSize = 28.sp,
                        modifier = Modifier.graphicsLayer {
                            if (!isUnlocked && !isHighlighted) alpha = 0.65f
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighlighted) (if (isDarkTheme) Color(0xFFFFD700) else Color(0xFF7A5800)) else if (isUnlocked && isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(bottom = 3.dp)
                                    .background(
                                        if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFF388E3C),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Text(
                                    text = "✨ Ver Festejo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isUnlocked && achievement.maxProgress > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedRatio.coerceIn(0.001f, 1f))
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${achievement.progress}/${achievement.maxProgress}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
