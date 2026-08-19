package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Scores / Leaderboard
    @Query("SELECT * FROM game_scores WHERE isWin = 1 ORDER BY timeInSeconds ASC LIMIT 50")
    fun getAllTopScores(): Flow<List<GameScoreEntity>>

    @Query("SELECT * FROM game_scores WHERE isWin = 1 AND difficultyName = :difficulty ORDER BY timeInSeconds ASC LIMIT 20")
    fun getTopScoresByDifficulty(difficulty: String): Flow<List<GameScoreEntity>>

    @Query("SELECT COUNT(*) FROM game_scores WHERE isWin = 1")
    fun getWinCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_scores")
    fun getTotalGamesPlayed(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScoreEntity)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialAchievements(achievements: List<AchievementEntity>)

    // Saved Game (Offline Resume)
    @Query("SELECT * FROM saved_game WHERE id = 1 LIMIT 1")
    suspend fun getSavedGame(): SavedGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(savedGame: SavedGameEntity)

    @Query("DELETE FROM saved_game WHERE id = 1")
    suspend fun clearSavedGame()
}
