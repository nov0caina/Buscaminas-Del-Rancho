package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val difficultyName: String,
    val timeInSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val isWin: Boolean,
    val playerName: String = "Vaquero Sinaloense"
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long = 0,
    val progress: Int = 0,
    val maxProgress: Int = 1
)

@Entity(tableName = "saved_game")
data class SavedGameEntity(
    @PrimaryKey val id: Int = 1,
    val difficultyName: String,
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val timeElapsedSeconds: Int,
    val boardJson: String, // Serialized cell grid
    val gameStatus: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)
