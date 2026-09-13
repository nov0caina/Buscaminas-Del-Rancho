package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.local.AchievementEntity
import com.example.ui.screens.LeatherStitchDefaults
import com.example.ui.screens.bounceClick
import com.example.ui.screens.leatherStitchBorder
import com.example.ui.screens.shimmerGoldenSweep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AchievementDetailModal(
    achievement: AchievementEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val lore = remember(achievement.id) { RanchAchievementLore.getLore(achievement.id) }
    val scope = rememberCoroutineScope()

    val scaleAnim = remember { Animatable(0.35f) }
    val alphaAnim = remember { Animatable(0f) }
    val scrimAlpha = remember { Animatable(0f) }

    var isClosing by remember { mutableStateOf(false) }

    val formattedDate = remember(achievement.unlockedTimestamp) {
        if (achievement.unlockedTimestamp > 0) {
            val sdf = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX"))
            sdf.format(Date(achievement.unlockedTimestamp))
        } else {
            "¡Recién desbloqueado!"
        }
    }

    val dismissAction: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            soundManager.playAchievementCardCloseSound()
            scope.launch {
                launch {
                    scaleAnim.animateTo(
                        targetValue = 0.65f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    alphaAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    scrimAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    )
                }
                delay(190L)
                onDismiss()
            }
        }
    }

    // Intercept hardware/gesture Back button
    BackHandler(enabled = !isClosing) {
        dismissAction()
    }

    // Entrance Animation & Sound Trigger
    LaunchedEffect(achievement.id) {
        soundManager.playAchievementCardOpenSound(achievement.id)
        launch {
            scrimAlpha.animateTo(
                targetValue = 0.72f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Modal Overlay Root
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("achievement_detail_modal_root"),
        contentAlignment = Alignment.Center
    ) {
        // Scrim / Backdrop with dismiss on click
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = scrimAlpha.value }
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissAction
                )
        )

        // Themed Modal Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isDarkTheme) Color(0xFF2C201A) else Color(0xFFFFFDF7)
                )
                .shimmerGoldenSweep(durationMillis = 2400)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA000),
                            Color(0xFFFFE082),
                            Color(0xFFFFD700)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .leatherStitchBorder(
                    color = LeatherStitchDefaults.GoldStitch,
                    cornerRadius = 24.dp,
                    inset = 4.dp,
                    dashLength = 4.5.dp,
                    gapLength = 3.dp
                )
                .padding(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume clicks inside card
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar: Title, Badge and Chunky Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD700).copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🏆 ${lore.badgeName.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkTheme) Color(0xFFFFD700) else Color(0xFFB78103),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.35f),
                                    offset = Offset(1f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            fontWeight = FontWeight.Black,
                            color = if (isDarkTheme) Color(0xFFFFE082) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Chunky 48dp Close Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .bounceClick(onClick = dismissAction)
                            .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                            .padding(bottom = 3.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF4A352D) else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar festejo de logro",
                            tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meme / Themed Art Animation in Center
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = if (isDarkTheme) 0.25f else 0.35f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AchievementMemeArt(
                        achievementId = achievement.id
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Authentic Sinaloense Lore Phrase
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDarkTheme) Color(0xFF1E1612) else Color(0xFFFFF3E0),
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (isDarkTheme) Color(0xFF5D4037) else Color(0xFFFFCC80),
                            RoundedCornerShape(16.dp)
                        )
                        .leatherStitchBorder(
                            color = if (isDarkTheme) LeatherStitchDefaults.AmberStitch.copy(alpha = 0.45f)
                            else LeatherStitchDefaults.GoldStitch.copy(alpha = 0.5f),
                            cornerRadius = 16.dp,
                            inset = 3.dp
                        )
                        .padding(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "\"${lore.sinaloaPhrase}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = if (isDarkTheme) Color(0xFFFFECB3) else Color(0xFF5D4037),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Achievement Description & Unlock Date
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "🗓️ $formattedDate",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color(0xFFFFD166) else Color(0xFF8D6E63)
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

