package com.example.data.model

enum class GameDifficulty(
    val displayName: String,
    val subtitle: String,
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val iconEmoji: String
) {
    PRINCIPIANTE("Modo Peladito (Fácil)", "9x9 • 10 Minas (El Porvenir)", 9, 9, 10, "🌵"),
    INTERMEDIO("Modo Alterado (Intermedio)", "14x16 • 40 Minas (La Sierra)", 16, 14, 35, "🤠"),
    EXPERTO("Modo Belikon (Dificil)", "14x30 • 99 Minas (Mina del Patrón)", 30, 14, 99, "⛏️"),
    PERSONALIZADA("Personalizado", "Tablero a tu gusto", 10, 10, 15, "⚙️");

    companion object {
        fun createCustom(rows: Int, cols: Int, mines: Int): CustomDifficulty {
            val validRows = rows.coerceIn(5, 30)
            val validCols = cols.coerceIn(5, 20)
            val maxMines = (validRows * validCols * 0.8).toInt().coerceAtLeast(1)
            val validMines = mines.coerceIn(1, maxMines)
            return CustomDifficulty(validRows, validCols, validMines)
        }
    }
}

data class CustomDifficulty(
    val rows: Int,
    val cols: Int,
    val mines: Int
)
