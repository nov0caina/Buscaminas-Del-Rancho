package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AchievementEntity
import com.example.ui.screens.bounceClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun AchievementUnlockOverlay(
    unlockEvents: SharedFlow<AchievementEntity>,
    modifier: Modifier = Modifier,
    onAchievementClick: (AchievementEntity) -> Unit = {}
) {
    val queue = remember { mutableStateListOf<AchievementEntity>() }
    var currentAchievement by remember { mutableStateOf<AchievementEntity?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    // Listen to new achievement unlock events and append to FIFO queue
    LaunchedEffect(unlockEvents) {
        unlockEvents.collect { achievement ->
            queue.add(achievement)
        }
    }

    // Process FIFO queue one by one
    LaunchedEffect(currentAchievement, queue.size, isVisible) {
        if (!isVisible && currentAchievement == null && queue.isNotEmpty()) {
            currentAchievement = queue.removeAt(0)
            isVisible = true
        }
    }

    // Auto-dismiss current achievement after duration
    LaunchedEffect(currentAchievement) {
        if (currentAchievement != null) {
            delay(3500L)
            isVisible = false
            delay(350L) // Wait for exit animation to finish
            currentAchievement = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible && currentAchievement != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            currentAchievement?.let { achievement ->
                AchievementUnlockToastCard(
                    achievement = achievement,
                    onClick = {
                        onAchievementClick(achievement)
                        isVisible = false
                        currentAchievement = null
                    }
                )
            }
        }
    }
}

@Composable
private fun AchievementUnlockToastCard(
    achievement: AchievementEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("achievement_unlock_toast")
            .bounceClick(onClick = onClick)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(bottom = 5.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3E2D26),
                        Color(0xFF261B16)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD166),
                        Color(0xFFF4A261),
                        Color(0xFFFFD166)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Avatar with Golden Bevel
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                    .padding(bottom = 3.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD166),
                                Color(0xFFE76F51)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconEmoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏆 ¡LOGRO DESBLOQUEADO!",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD166),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "✨",
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
