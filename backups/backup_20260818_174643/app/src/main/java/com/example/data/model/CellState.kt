package com.example.data.model

data class CellState(
    val row: Int,
    val col: Int,
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val isQuestion: Boolean = false,
    val adjacentMines: Int = 0,
    val isExploded: Boolean = false
)

enum class GameStatus {
    IDLE,       // Board created, waiting for first click
    PLAYING,    // Timer active
    WON,        // All safe cells revealed
    LOST        // Mine detonated
}

enum class ClickMode {
    REVEAL,     // Tap opens cell
    FLAG        // Tap toggles flag (Sombrero)
}

enum class VaqueroFace {
    HAPPY,      // 🤠 Normal playing
    SUSPENSE,   // 😲 While pressing cell
    DEAD,       // 😵 Game over loss
    VICTORIOUS  // 🏆 Game won
}
