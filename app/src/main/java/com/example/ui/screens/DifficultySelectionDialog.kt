package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GameDifficulty

@Composable
fun DifficultySelectionDialog(
    onDismiss: () -> Unit,
    onSelectDifficulty: (difficulty: GameDifficulty, customRows: Int, customCols: Int, customMines: Int) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isPatronUnlocked: Boolean = false,
    onRequestUnlockPatron: () -> Unit = {}
) {
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.PRINCIPIANTE) }

    var customRows by remember { mutableFloatStateOf(10f) }
    var customCols by remember { mutableFloatStateOf(10f) }
    var customMines by remember { mutableFloatStateOf(15f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(bottom = 6.dp)
                .background(
                    if (isDarkTheme) Color(0xFF2C221E) else MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(24.dp)
                )
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤠 Selecciona la Dificultad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GameDifficulty.entries.forEach { diff ->
                    val isLocked = !isPatronUnlocked && (diff == GameDifficulty.EXPERTO || diff == GameDifficulty.PERSONALIZADA)
                    val isSelected = selectedDifficulty == diff
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isLocked) {
                                    onRequestUnlockPatron()
                                } else {
                                    selectedDifficulty = diff
                                }
                            }
                            .testTag("diff_card_${diff.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.primaryContainer
                            } else {
                                if (isDarkTheme) Color(0xFF1E1714) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLocked) {
                                Text(
                                    text = "🔒",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(start = 6.dp, end = 2.dp)
                                )
                            } else {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedDifficulty = diff }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diff.iconEmoji,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = diff.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected && isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isLocked) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "👑 VIP",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFFFD700),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isLocked) "Pase del Patrón • 99 Minas / Libre" else diff.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLocked) Color(0xFFFFD700).copy(alpha = 0.9f) else if (isDarkTheme) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Custom settings options if PERSONALIZADA is chosen
                if (selectedDifficulty == GameDifficulty.PERSONALIZADA) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val maxMines = ((customRows * customCols) * 0.8f).coerceAtLeast(1f)
                    val totalCells = (customRows * customCols).toInt()
                    val currentMines = customMines.toInt().coerceAtMost(maxMines.toInt())
                    val density = if (totalCells > 0) (currentMines * 100 / totalCells) else 0

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomSliderRow(
                            icon = "📏",
                            label = "Filas",
                            value = customRows,
                            range = 5f..25f,
                            onValueChange = { customRows = it },
                            isDarkTheme = isDarkTheme
                        )

                        CustomSliderRow(
                            icon = "📐",
                            label = "Columnas",
                            value = customCols,
                            range = 5f..16f,
                            onValueChange = { customCols = it },
                            isDarkTheme = isDarkTheme
                        )

                        CustomSliderRow(
                            icon = "💣",
                            label = "Minas",
                            value = customMines.coerceAtMost(maxMines),
                            range = 1f..maxMines,
                            onValueChange = { customMines = it },
                            isDarkTheme = isDarkTheme
                        )

                        // Summary Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(bottom = 2.dp)
                                .background(
                                    if (isDarkTheme) Color(0xFF1E1714) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▦ Total: $totalCells casillas • $currentMines minas ($density% peligro)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .bounceClick(onClick = onDismiss)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_confirm_difficulty")
                            .bounceClick(onClick = {
                                val maxMines = ((customRows * customCols) * 0.8f).coerceAtLeast(1f)
                                onSelectDifficulty(
                                    selectedDifficulty,
                                    customRows.toInt(),
                                    customCols.toInt(),
                                    customMines.toInt().coerceAtMost(maxMines.toInt())
                                )
                            })
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "¡A Jugar! ⛏️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDifficultyDialog(
    onDismiss: () -> Unit,
    onConfirmCustom: (customRows: Int, customCols: Int, customMines: Int) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    var customRows by remember { mutableFloatStateOf(10f) }
    var customCols by remember { mutableFloatStateOf(10f) }
    var customMines by remember { mutableFloatStateOf(15f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(bottom = 6.dp)
                .background(
                    if (isDarkTheme) Color(0xFF2C221E) else MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(24.dp)
                )
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "⚙️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tablero Personalizado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val maxMines = ((customRows * customCols) * 0.8f).coerceAtLeast(1f)
                val totalCells = (customRows * customCols).toInt()
                val currentMines = customMines.toInt().coerceAtMost(maxMines.toInt())
                val density = if (totalCells > 0) (currentMines * 100 / totalCells) else 0

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomSliderRow(
                        icon = "📏",
                        label = "Filas",
                        value = customRows,
                        range = 5f..25f,
                        onValueChange = { customRows = it },
                        isDarkTheme = isDarkTheme
                    )

                    CustomSliderRow(
                        icon = "📐",
                        label = "Columnas",
                        value = customCols,
                        range = 5f..16f,
                        onValueChange = { customCols = it },
                        isDarkTheme = isDarkTheme
                    )

                    CustomSliderRow(
                        icon = "💣",
                        label = "Minas",
                        value = customMines.coerceAtMost(maxMines),
                        range = 1f..maxMines,
                        onValueChange = { customMines = it },
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Board Summary Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(bottom = 2.dp)
                        .background(
                            if (isDarkTheme) Color(0xFF1E1714) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▦ Total: $totalCells casillas • $currentMines minas ($density% peligro)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .bounceClick(onClick = onDismiss)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_confirm_custom")
                            .bounceClick(onClick = {
                                onConfirmCustom(
                                    customRows.toInt(),
                                    customCols.toInt(),
                                    currentMines
                                )
                            })
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "¡A Jugar! ⛏️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomSliderRow(
    icon: String,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(bottom = 3.dp)
            .background(
                if (isDarkTheme) Color(0xFF1E1714) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Numeric Badge
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(bottom = 2.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${value.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Minus
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .bounceClick(onClick = {
                            val nextVal = (value - 1f).coerceIn(range.start, range.endInclusive)
                            onValueChange(nextVal)
                        })
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(bottom = 2.dp)
                        .background(
                            if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = range,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = if (isDarkTheme) Color.Black.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Stepper Plus
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .bounceClick(onClick = {
                            val nextVal = (value + 1f).coerceIn(range.start, range.endInclusive)
                            onValueChange(nextVal)
                        })
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(bottom = 2.dp)
                        .background(
                            if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

