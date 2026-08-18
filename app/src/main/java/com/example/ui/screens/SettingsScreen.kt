package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmojiAnimationStyle
import com.example.data.model.RanchFlagIcon
import com.example.ui.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: GameUiState,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleDailyNotification: (Boolean) -> Unit,
    onSelectRanchFlagIcon: (RanchFlagIcon) -> Unit,
    onSelectEmojiAnimationStyle: (EmojiAnimationStyle) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "⚙️ Configuración",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Temática & Marcadores de Rancho",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de Ícono de Bandera / Marcador de Rancho (Sombrero, Cuernos, etc.)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        text = "Elige el símbolo campirano para marcar casillas sospechosas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val allIcons = RanchFlagIcon.values().toList()
                    val row1 = allIcons.take(5)
                    val row2 = allIcons.drop(5)

                    // Fila 1 de Íconos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row1.forEach { iconOption ->
                            val isSelected = uiState.ranchFlagIcon == iconOption
                            Surface(
                                onClick = { onSelectRanchFlagIcon(iconOption) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surface,
                                border = if (isSelected)
                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
                                else
                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .testTag("ranch_icon_${iconOption.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(2.dp),
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
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Fila 2 de Íconos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row2.forEach { iconOption ->
                            val isSelected = uiState.ranchFlagIcon == iconOption
                            Surface(
                                onClick = { onSelectRanchFlagIcon(iconOption) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surface,
                                border = if (isSelected)
                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
                                else
                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .testTag("ranch_icon_${iconOption.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(2.dp),
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
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selector de Estilo de Animación de Emojis
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estilo de Animación de Emojis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Elige cómo reacciona el emoji al fijar una marca en el tablero:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    EmojiAnimationStyle.values().forEach { styleOption ->
                        val isSelected = uiState.emojiAnimationStyle == styleOption
                        Surface(
                            onClick = { onSelectEmojiAnimationStyle(styleOption) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface,
                            border = if (isSelected)
                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("anim_style_${styleOption.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = styleOption.previewBadge,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = styleOption.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = styleOption.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Preferencias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Modo Rancho Noche Toggle
            SettingsToggleCard(
                title = "Modo Rancho Noche (Modo Oscuro)",
                subtitle = "Estética nocturna del desierto sinaloense",
                icon = Icons.Default.DarkMode,
                checked = uiState.isDarkTheme,
                onCheckedChange = onToggleDarkTheme,
                testTag = "switch_dark_theme"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vibración / Haptics Toggle
            SettingsToggleCard(
                title = "Vibración Táctil",
                subtitle = "Siente el espuelazo al marcar banderas o detonar",
                icon = Icons.Default.Vibration,
                checked = uiState.isHapticsEnabled,
                onCheckedChange = onToggleHaptics,
                testTag = "switch_haptics"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recordatorios Diarios Push Notification Toggle
            SettingsToggleCard(
                title = "Recordatorio Diario de Juego",
                subtitle = "Notificación push diaria para mantener activo el rancho",
                icon = Icons.Default.Notifications,
                checked = uiState.isDailyNotificationEnabled,
                onCheckedChange = onToggleDailyNotification,
                testTag = "switch_notifications"
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Información de la App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscaminas del Rancho v1.0",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Estilo Sinaloa mi pa 🤠\n" +
                                "• 100% Offline & Guarda Partidas automáticamente\n" +
                                "• Cero anuncios molestos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
