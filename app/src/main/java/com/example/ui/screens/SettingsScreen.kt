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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RanchFlagIcon
import com.example.ui.viewmodel.GameUiState
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.MysticSmokeParticleSystem
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SettingsScreen(
    uiState: GameUiState,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleDailyNotification: (Boolean) -> Unit,
    onSelectRanchFlagIcon: (RanchFlagIcon) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
        label = "bg_particles_settings"
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
                    translationY = -scrollState.value * 0.35f
                    translationX = (sin(progress * 2 * PI) * 30f).toFloat()
                    val scale = 1f + (sin(progress * PI) * 0.05f).toFloat()
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            if (uiState.isDarkTheme) {
                smokeParticleSystem.setupMysticSmoke(size.width / 2f, size.height / 2f, size.width * 0.35f)
                smokeParticleSystem.render(this, progress, size.width, size.height)
            } else {
                dustParticleSystem.setupAmbientDust(size.width, size.height)
                dustParticleSystem.render(this, progress)
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp) 
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
                    text = "Configuración",
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

            Spacer(modifier = Modifier.height(36.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Temática & Marcadores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Ícono de Bandera / Marcador de Rancho
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(bottom = 6.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ícono de Marcador (Distintivo)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,

                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Elige el símbolo campirano para marcar casillas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val allIcons = RanchFlagIcon.values()
                        val rows = allIcons.toList().chunked(5)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (rowIcons in rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (iconOption in rowIcons) {
                                        val isSelected = uiState.ranchFlagIcon == iconOption
                                        val lipSize by animateDpAsState(targetValue = if (isSelected) 0.dp else 4.dp, label = "lipSize")
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(68.dp)
                                                .testTag("ranch_icon_${iconOption.name.lowercase()}")
                                                .bounceClick(onClick = { onSelectRanchFlagIcon(iconOption) })
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
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = iconOption.emoji,
                                                    fontSize = 20.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = iconOption.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                    
                                    val emptySpots = 5 - rowIcons.size
                                    for (i in 0 until emptySpots) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Preferencias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleCard(
                    title = "Modo Rancho Noche",
                    subtitle = "Estética nocturna del desierto",
                    icon = Icons.Default.DarkMode,
                    checked = uiState.isDarkTheme,
                    onCheckedChange = onToggleDarkTheme,
                    testTag = "switch_dark_theme"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsToggleCard(
                    title = "Vibración Táctil",
                    subtitle = "Siente el espuelazo al detonar",
                    icon = Icons.Default.Vibration,
                    checked = uiState.isHapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    testTag = "switch_haptics"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsToggleCard(
                    title = "Recordatorio Diario",
                    subtitle = "Alerta push para limpiar tu rancho",
                    icon = Icons.Default.Notifications,
                    checked = uiState.isDailyNotificationEnabled,
                    onCheckedChange = onToggleDailyNotification,
                    testTag = "switch_notifications"
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Acerca De",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(bottom = 6.dp)
                        .background(Color(0xFF4E342E), RoundedCornerShape(16.dp)) // Dark wood/leather
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFFB300) // Gold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buscaminas del Rancho v1.0",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = """• Estilo Sinaloa mi pa 🤠
• Guarda partidas offline
• Cero anuncios.""",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .bounceClick(onClick = { onCheckedChange(!checked) })
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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

            Spacer(modifier = Modifier.width(12.dp))

            MechanicalToggleSwitch(checked = checked)
        }
    }
}

@Composable
private fun MechanicalToggleSwitch(checked: Boolean) {
    val thumbOffset by animateDpAsState(targetValue = if (checked) 24.dp else 2.dp, label = "thumbOffset")
    val trackColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    
    // Track (Ranura)
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(32.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)) // Sombra interna pista
            .padding(top = 2.dp, start = 2.dp, end = 2.dp, bottom = 2.dp)
            .background(trackColor, RoundedCornerShape(6.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
    ) {
        // Thumb (Botón deslizable mecánico 3D)
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
                .align(Alignment.CenterStart)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(4.dp)) // Sombra/Labio del botón
                .padding(bottom = 3.dp)
                .background(Color.White, RoundedCornerShape(4.dp))
        )
    }
}
