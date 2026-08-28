package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AchievementEntity
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.MysticSmokeParticleSystem
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val listState = rememberLazyListState()

    val dustParticleSystem = remember { DustParticleSystem(150) }
    val smokeParticleSystem = remember { MysticSmokeParticleSystem(150) }

    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
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
                smokeParticleSystem.setupMysticSmoke(size.width / 2f, size.height / 2f, size.width * 0.35f)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    text = "Logros",
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

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementCard(achievement = achievement, isDarkTheme = isDarkTheme)
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: AchievementEntity, isDarkTheme: Boolean) {
    val isUnlocked = achievement.isUnlocked
    val surfaceCol = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("achievement_card_${achievement.id}")
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 6.dp)
            .background(surfaceCol, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    .padding(bottom = 4.dp)
                    .background(
                        if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconEmoji,
                    fontSize = 28.sp
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
                        color = if (isUnlocked && isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                    )

                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(bottom = 3.dp)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "¡DESBLOQUEADO!",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(10.dp)) {
                            // Track
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(5.dp)))
                            // Progress
                            Box(modifier = Modifier
                                .fillMaxWidth(achievement.progress.toFloat() / achievement.maxProgress)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
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
