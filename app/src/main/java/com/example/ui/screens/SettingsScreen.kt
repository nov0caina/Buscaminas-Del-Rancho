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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.ui.viewmodel.ThemeMode
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.RanchoSmokeParticleSystem
import com.example.ui.theme.RanchoCactusSecondary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SettingsScreen(
    uiState: GameUiState,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isPatronUnlocked: Boolean = false,
    patronPrice: String = "$25.00 MXN",
    onSelectThemeMode: (ThemeMode) -> Unit = {},
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onToggleHaptics: (Boolean) -> Unit,
    onToggleDailyNotification: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onToggleSfx: (Boolean) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onSelectRanchFlagIcon: (RanchFlagIcon) -> Unit,
    onOpenPatronPassDialog: () -> Unit = {},
    onNavigateToCredits: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onToggleDailyNotification(isGranted)
    }

    val scrollState = rememberScrollState()

    val dustParticleSystem = remember { DustParticleSystem(150) }
    val smokeParticleSystem = remember { RanchoSmokeParticleSystem(150) }

    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
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
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .leatherStitchBorder(
                            color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.4f)
                            else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.4f),
                            cornerRadius = 12.dp,
                            inset = 2.5.dp,
                            dashLength = 3.5.dp,
                            gapLength = 2.5.dp
                        ),
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
                Column(modifier = Modifier.staggeredEntrance(index = 0)) {
                    Text(
                        text = "Temática & Marcadores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(bottom = 6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                            .leatherStitchBorder(
                                color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.35f)
                                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.35f),
                                cornerRadius = 20.dp
                            )
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
                                            val isLocked = iconOption.isVip && !isPatronUnlocked
                                            val isSelected = uiState.ranchFlagIcon == iconOption
                                            val lipSize by animateDpAsState(targetValue = if (isSelected) 0.dp else 4.dp, label = "lipSize")
                                            
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(68.dp)
                                                    .testTag("ranch_icon_${iconOption.name.lowercase()}")
                                                    .bounceClick(onClick = {
                                                        if (isLocked) {
                                                            onOpenPatronPassDialog()
                                                        } else {
                                                            onSelectRanchFlagIcon(iconOption)
                                                        }
                                                    })
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Black.copy(alpha = 0.25f),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .padding(bottom = lipSize)
                                                    .background(
                                                        if (isSelected) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surface
                                                        },
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .border(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                                    .leatherStitchBorder(
                                                        color = if (isSelected) {
                                                            if (isDarkTheme) LeatherStitchDefaults.GoldStitch.copy(alpha = 0.6f)
                                                            else Color.White.copy(alpha = 0.5f)
                                                        } else {
                                                            if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.25f)
                                                            else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.25f)
                                                        },
                                                        cornerRadius = 14.dp,
                                                        inset = 2.5.dp,
                                                        dashLength = 3.dp,
                                                        gapLength = 2.dp
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = if (isLocked) "🔒" else iconOption.emoji,
                                                        fontSize = 24.sp
                                                    )
                                                    if (iconOption.isVip) {
                                                        Text(
                                                            text = "VIP",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (isDarkTheme) Color(0xFFFFD700) else Color(0xFF8B5E3C)
                                                        )
                                                    }
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
                }

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.staggeredEntrance(index = 1)) {
                    Text(
                        text = "Sonido & Música",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsAudioCard(
                        title = "Música de Fondo",
                        subtitle = "Banda y corridos sinaloenses",
                        icon = Icons.Default.MusicNote,
                        enabled = uiState.isMusicEnabled,
                        onToggleEnabled = onToggleMusic,
                        volume = uiState.musicVolume,
                        onVolumeChange = onMusicVolumeChange,
                        testTag = "card_music_volume"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsAudioCard(
                        title = "Efectos de Sonido (SFX)",
                        subtitle = "Pops, explosiones y fanfarrias",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        enabled = uiState.isSfxEnabled,
                        onToggleEnabled = onToggleSfx,
                        volume = uiState.sfxVolume,
                        onVolumeChange = onSfxVolumeChange,
                        testTag = "card_sfx_volume"
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.staggeredEntrance(index = 2)) {
                    Text(
                        text = "Preferencias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(bottom = 6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                            .leatherStitchBorder(
                                color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.35f)
                                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.35f),
                                cornerRadius = 20.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tema Día / Noche",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val subtitleText = when (uiState.themeMode) {
                                        ThemeMode.SYSTEM -> if (isDarkTheme) "Sincronizado: Noche del Desierto 🌙" else "Sincronizado: Sol Campirano ☀️"
                                        ThemeMode.LIGHT -> "Fijo: Sol Campirano ☀️"
                                        ThemeMode.DARK -> "Fijo: Noche del Desierto 🌙"
                                    }
                                    Text(
                                        text = subtitleText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    val isSelected = uiState.themeMode == mode

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("btn_theme_${mode.name.lowercase()}")
                                            .bounceClick(onClick = { onSelectThemeMode(mode) })
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.20f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(bottom = if (isSelected) 1.dp else 4.dp)
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surface
                                                },
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.4f) 
                                                        else if (isDarkTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .leatherStitchBorder(
                                                color = if (isSelected) {
                                                    if (isDarkTheme) LeatherStitchDefaults.GoldStitch.copy(alpha = 0.55f)
                                                    else Color.White.copy(alpha = 0.5f)
                                                } else {
                                                    if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.25f)
                                                    else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.25f)
                                                },
                                                cornerRadius = 12.dp,
                                                inset = 2.5.dp,
                                                dashLength = 3.5.dp,
                                                gapLength = 2.5.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = mode.emoji, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsToggleCard(
                    title = "Vibración Táctil",
                    subtitle = "Siente el espuelazo al detonar",
                    icon = Icons.Default.Vibration,
                    checked = uiState.isHapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    testTag = "switch_haptics",
                    modifier = Modifier.staggeredEntrance(index = 3)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsToggleCard(
                    title = "Recordatorio Diario",
                    subtitle = "Alerta push para limpiar tu rancho",
                    icon = Icons.Default.Notifications,
                    checked = uiState.isDailyNotificationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onToggleDailyNotification(true)
                                }
                            } else {
                                onToggleDailyNotification(true)
                            }
                        } else {
                            onToggleDailyNotification(false)
                        }
                    },
                    testTag = "switch_notifications",
                    modifier = Modifier.staggeredEntrance(index = 4)
                )

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.staggeredEntrance(index = 5)) {
                    Text(
                        text = "Acerca De",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive 3D Acerca De / Créditos Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_about_credits")
                            .bounceClick(onClick = onNavigateToCredits)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(bottom = 6.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF332B25) else Color(0xFF8D6E63),
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFFFB300).copy(alpha = if (isDarkTheme) 0.4f else 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .leatherStitchBorder(
                                color = LeatherStitchDefaults.GoldStitch.copy(alpha = 0.5f),
                                cornerRadius = 16.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300) // Gold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Buscaminas del Rancho v1.0",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Ver créditos, equipo y colaboradores 🤠",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFFD166)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "Ver créditos",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
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
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .bounceClick(onClick = { onCheckedChange(!checked) })
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isDark) Color(0xFF5D4037).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .leatherStitchBorder(
                color = if (isDark) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.35f)
                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.35f),
                cornerRadius = 16.dp
            )
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
    val isDark = isSystemInDarkTheme()
    val thumbOffset by animateDpAsState(targetValue = if (checked) 24.dp else 2.dp, label = "thumbOffset")
    val trackColor = if (checked) {
        if (isDark) MaterialTheme.colorScheme.primary else RanchoCactusSecondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
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

@Composable
private fun SettingsAudioCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(bottom = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isDark) Color(0xFF5D4037).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .leatherStitchBorder(
                color = if (isDark) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.35f)
                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.35f),
                cornerRadius = 16.dp
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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

                Box(
                    modifier = Modifier.bounceClick(onClick = { onToggleEnabled(!enabled) })
                ) {
                    MechanicalToggleSwitch(checked = enabled)
                }
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD166),
                            activeTrackColor = Color(0xFFF4A261),
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )

                    val isDarkSlider = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .background(if (isDarkSlider) Color(0xFF2C221E) else MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .border(1.dp, if (isDarkSlider) Color(0xFFFFD166).copy(alpha = 0.6f) else Color(0xFFD4AF37).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkSlider) Color(0xFFFFD166) else Color.White
                        )
                    }
                }
            }
        }
    }
}
