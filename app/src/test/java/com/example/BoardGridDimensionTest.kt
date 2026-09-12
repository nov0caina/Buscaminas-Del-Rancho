package com.example

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar la integridad y simetría dimensional de la cuadrícula
 * en tableros de diversas dificultades y densidades de pantalla.
 */
class BoardGridDimensionTest {

    @Test
    fun `pixel coherent grid width guarantees zero deficit for all columns including last column`() {
        val testDensities = listOf(1.5f, 2.0f, 2.25f, 2.5f, 2.625f, 2.75f, 2.8125f, 3.0f, 3.5f, 4.0f)
        val testCols = listOf(9, 10, 14, 16, 20, 30)
        val cellDpCandidates = listOf(24.dp, 26.dp, 28.dp, 30.dp, 32.dp, 36.dp, 40.dp)
        val spacingDp = 2.dp

        for (d in testDensities) {
            val density = Density(density = d)
            for (cols in testCols) {
                for (rawCellDp in cellDpCandidates) {
                    val cellPx = with(density) { rawCellDp.roundToPx() }
                    val spacingPx = with(density) { spacingDp.roundToPx() }

                    // Nueva fórmula píxel-coherente
                    val gridWidthPx = (cellPx * cols) + (spacingPx * (cols - 1))
                    val gridWidthDp = with(density) { gridWidthPx.toDp() }
                    val remeasuredGridWidthPx = with(density) { gridWidthDp.roundToPx() }

                    // Verificación de biyección exacta
                    assertEquals(
                        "Fallo de biyección dp <-> px en densidad $d para $cols columnas",
                        gridWidthPx,
                        remeasuredGridWidthPx
                    )

                    // Verificación de que la última columna recibe exactamente cellPx sin truncamiento
                    val allocatedPrefix = (cols - 1) * cellPx
                    val totalSpacing = (cols - 1) * spacingPx
                    val remainingForLastCol = remeasuredGridWidthPx - totalSpacing - allocatedPrefix

                    assertEquals(
                        "Déficit detectado en la última columna para densidad $d, cols $cols, cellDp $rawCellDp",
                        cellPx,
                        remainingForLastCol
                    )
                }
            }
        }
    }

    @Test
    fun `legacy float formula demonstrates defect on 14 columns at 2_75 density while new formula fixes it`() {
        val density = Density(density = 2.75f)
        val cols = 14
        val rawCellDp = 24.dp
        val spacingDp = 2.dp

        // Comportamiento anterior con fórmula de coma flotante
        val cellPx = with(density) { rawCellDp.roundToPx() }
        val spacingPx = with(density) { spacingDp.roundToPx() }
        val legacyGridWidthDp = (rawCellDp * cols) + (spacingDp * (cols - 1))
        val legacyGridWidthPx = with(density) { legacyGridWidthDp.roundToPx() }

        val allocatedPrefix = (cols - 1) * cellPx
        val totalSpacing = (cols - 1) * spacingPx
        val legacyRemainingForLastCol = legacyGridWidthPx - totalSpacing - allocatedPrefix

        // Confirma que la fórmula anterior dejaba un déficit negativo (celda derecha aplastada)
        assertTrue(
            "La fórmula anterior debía presentar déficit en la celda 14",
            legacyRemainingForLastCol < cellPx
        )

        // Comportamiento corregido píxel-coherente
        val newGridWidthPx = (cellPx * cols) + (spacingPx * (cols - 1))
        val newGridWidthDp = with(density) { newGridWidthPx.toDp() }
        val remeasuredNewGridWidthPx = with(density) { newGridWidthDp.roundToPx() }
        val newRemainingForLastCol = remeasuredNewGridWidthPx - totalSpacing - allocatedPrefix

        // Confirma que la nueva fórmula resuelve el déficit con paridad exacta
        assertEquals(cellPx, newRemainingForLastCol)
    }
}

