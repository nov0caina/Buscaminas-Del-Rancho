package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GameDifficulty

@Composable
fun DifficultySelectionDialog(
    onDismiss: () -> Unit,
    onSelectDifficulty: (difficulty: GameDifficulty, customRows: Int, customCols: Int, customMines: Int) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.PRINCIPIANTE) }

    var customRows by remember { mutableFloatStateOf(10f) }
    var customCols by remember { mutableFloatStateOf(10f) }
    var customMines by remember { mutableFloatStateOf(15f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤠 Selecciona la Dificultad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GameDifficulty.entries.forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedDifficulty = diff }
                            .testTag("diff_card_${diff.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDifficulty = diff }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diff.iconEmoji,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = diff.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = diff.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Custom settings options if PERSONALIZADA is chosen
                if (selectedDifficulty == GameDifficulty.PERSONALIZADA) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Filas: ${customRows.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = customRows,
                            onValueChange = { customRows = it },
                            valueRange = 5f..25f,
                            steps = 20
                        )

                        Text(
                            text = "Columnas: ${customCols.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = customCols,
                            onValueChange = { customCols = it },
                            valueRange = 5f..16f,
                            steps = 11
                        )

                        val maxMines = ((customRows * customCols) * 0.8f).coerceAtLeast(1f)
                        Text(
                            text = "Minas: ${customMines.toInt().coerceAtMost(maxMines.toInt())}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = customMines.coerceAtMost(maxMines),
                            onValueChange = { customMines = it },
                            valueRange = 1f..maxMines
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSelectDifficulty(
                                selectedDifficulty,
                                customRows.toInt(),
                                customCols.toInt(),
                                customMines.toInt()
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_confirm_difficulty")
                    ) {
                        Text("¡A Jugar! ⛏️", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDifficultyDialog(
    onDismiss: () -> Unit,
    onConfirmCustom: (customRows: Int, customCols: Int, customMines: Int) -> Unit
) {
    var customRows by remember { mutableFloatStateOf(10f) }
    var customCols by remember { mutableFloatStateOf(10f) }
    var customMines by remember { mutableFloatStateOf(15f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚙️ Tablero Personalizado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Filas: ${customRows.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = customRows,
                        onValueChange = { customRows = it },
                        valueRange = 5f..25f,
                        steps = 20
                    )

                    Text(
                        text = "Columnas: ${customCols.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = customCols,
                        onValueChange = { customCols = it },
                        valueRange = 5f..16f,
                        steps = 11
                    )

                    val maxMines = ((customRows * customCols) * 0.8f).coerceAtLeast(1f)
                    Text(
                        text = "Minas: ${customMines.toInt().coerceAtMost(maxMines.toInt())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = customMines.coerceAtMost(maxMines),
                        onValueChange = { customMines = it },
                        valueRange = 1f..maxMines
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val maxMines = ((customRows * customCols) * 0.8f).coerceAtLeast(1f)
                            onConfirmCustom(
                                customRows.toInt(),
                                customCols.toInt(),
                                customMines.toInt().coerceAtMost(maxMines.toInt())
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_confirm_custom")
                    ) {
                        Text("¡A Jugar! ⛏️", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
