package com.example.data.repository

import com.example.data.local.AchievementEntity
import com.example.data.local.GameDao
import com.example.data.local.GameScoreEntity
import com.example.data.local.SavedGameEntity
import com.example.data.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class GlobalLeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val location: String,
    val timeSeconds: Int,
    val difficulty: String,
    val avatarEmoji: String
)

class GameRepository(private val gameDao: GameDao) {

    val topScores: Flow<List<GameScoreEntity>> = gameDao.getAllTopScores()
    val allAchievements: Flow<List<AchievementEntity>> = gameDao.getAllAchievements()
    val totalGamesPlayed: Flow<Int> = gameDao.getTotalGamesPlayed()
    val winCount: Flow<Int> = gameDao.getWinCount()

    fun getTopScoresByDifficulty(difficulty: String): Flow<List<GameScoreEntity>> {
        return gameDao.getTopScoresByDifficulty(difficulty)
    }

    suspend fun initDefaultAchievementsIfNeeded() {
        val defaults = listOf(
            AchievementEntity(
                id = "first_win",
                title = "Primer Espuelazo 🤠",
                description = "Gana tu primera partida de Buscaminas del Rancho.",
                iconEmoji = "🤠",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "patron_experto",
                title = "El Patrón del Rancho 👑",
                description = "Gana una partida en dificultad Experto.",
                iconEmoji = "👑",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "fast_hand",
                title = "Rápido como el Viento ⚡",
                description = "Gana en Principiante en menos de 60 segundos.",
                iconEmoji = "⚡",
                maxProgress = 1
            ),
            AchievementEntity(
                id = "cazador_iguanas",
                title = "Cazador de Iguanas 🦎",
                description = "Gana 5 partidas en cualquier dificultad.",
                iconEmoji = "🦎",
                maxProgress = 5
            ),
            AchievementEntity(
                id = "minero_veterano",
                title = "Minero Sinaloense ⛏️",
                description = "Gana 10 partidas en el rancho.",
                iconEmoji = "⛏️",
                maxProgress = 10
            ),
            AchievementEntity(
                id = "sin_banderas",
                title = "A Ojo de Buen Cubero 👁️",
                description = "Gana una partida sin haber colocado banderas.",
                iconEmoji = "👁️",
                maxProgress = 1
            )
        )
        gameDao.insertInitialAchievements(defaults)
    }

    suspend fun recordGameFinished(
        difficulty: GameDifficulty,
        timeSeconds: Int,
        isWin: Boolean,
        rows: Int,
        cols: Int,
        mines: Int,
        flagsPlaced: Int
    ): List<AchievementEntity> {
        val score = GameScoreEntity(
            difficultyName = difficulty.displayName,
            timeInSeconds = timeSeconds,
            rows = rows,
            cols = cols,
            mines = mines,
            isWin = isWin
        )
        gameDao.insertScore(score)

        return if (isWin) {
            checkAndUnlockAchievements(difficulty, timeSeconds, flagsPlaced)
        } else {
            emptyList()
        }
    }

    private suspend fun checkAndUnlockAchievements(
        difficulty: GameDifficulty,
        timeSeconds: Int,
        flagsPlaced: Int
    ): List<AchievementEntity> {
        val currentAchievements = gameDao.getAllAchievements().firstOrNull() ?: emptyList()
        val wins = (gameDao.getWinCount().firstOrNull() ?: 0) + 1
        val newlyUnlocked = mutableListOf<AchievementEntity>()

        for (achievement in currentAchievements) {
            var updated = achievement
            when (achievement.id) {
                "first_win" -> {
                    if (!achievement.isUnlocked) {
                        updated = achievement.copy(isUnlocked = true, unlockedTimestamp = System.currentTimeMillis(), progress = 1)
                    }
                }
                "patron_experto" -> {
                    if (difficulty == GameDifficulty.EXPERTO && !achievement.isUnlocked) {
                        updated = achievement.copy(isUnlocked = true, unlockedTimestamp = System.currentTimeMillis(), progress = 1)
                    }
                }
                "fast_hand" -> {
                    if (difficulty == GameDifficulty.PRINCIPIANTE && timeSeconds <= 60 && !achievement.isUnlocked) {
                        updated = achievement.copy(isUnlocked = true, unlockedTimestamp = System.currentTimeMillis(), progress = 1)
                    }
                }
                "cazador_iguanas" -> {
                    val newProg = wins.coerceAtMost(achievement.maxProgress)
                    val unlocked = newProg >= achievement.maxProgress
                    updated = achievement.copy(progress = newProg, isUnlocked = unlocked, unlockedTimestamp = if (unlocked) System.currentTimeMillis() else 0)
                }
                "minero_veterano" -> {
                    val newProg = wins.coerceAtMost(achievement.maxProgress)
                    val unlocked = newProg >= achievement.maxProgress
                    updated = achievement.copy(progress = newProg, isUnlocked = unlocked, unlockedTimestamp = if (unlocked) System.currentTimeMillis() else 0)
                }
                "sin_banderas" -> {
                    if (flagsPlaced == 0 && !achievement.isUnlocked) {
                        updated = achievement.copy(isUnlocked = true, unlockedTimestamp = System.currentTimeMillis(), progress = 1)
                    }
                }
            }

            if (updated != achievement) {
                gameDao.insertOrUpdateAchievement(updated)
                if (!achievement.isUnlocked && updated.isUnlocked) {
                    newlyUnlocked.add(updated)
                }
            }
        }
        return newlyUnlocked
    }

    suspend fun saveActiveGame(
        difficultyName: String,
        rows: Int,
        cols: Int,
        mines: Int,
        timeSeconds: Int,
        boardJson: String,
        gameStatus: String
    ) {
        val savedGame = SavedGameEntity(
            difficultyName = difficultyName,
            rows = rows,
            cols = cols,
            mines = mines,
            timeElapsedSeconds = timeSeconds,
            boardJson = boardJson,
            gameStatus = gameStatus
        )
        gameDao.saveGame(savedGame)
    }

    suspend fun getSavedGame(): SavedGameEntity? = gameDao.getSavedGame()

    suspend fun clearSavedGame() = gameDao.clearSavedGame()

    // Simulated Sinaloa Global Leaderboard (Google Play Games Global Standings)
    fun getGlobalSinaloaLeaderboard(difficulty: GameDifficulty): List<GlobalLeaderboardEntry> {
        val baseTimes = when (difficulty) {
            GameDifficulty.PRINCIPIANTE -> listOf(12, 18, 25, 31, 42, 50, 65, 80, 95, 110)
            GameDifficulty.INTERMEDIO -> listOf(85, 102, 120, 145, 170, 195, 210, 240, 280, 310)
            GameDifficulty.EXPERTO -> listOf(210, 245, 280, 320, 360, 410, 460, 520, 590, 650)
            GameDifficulty.PERSONALIZADA -> listOf(45, 60, 75, 90, 120, 150, 180, 210, 240, 300)
        }

        val vaqueros = listOf(
            Pair("Kakito13", "Guasave ⚾"),
            Pair("TheDustfinger", "Guasave ⚾"),
            Pair("nov0caina", "Culiacán 🤠⛏️"),
            Pair("Pancho Cachondo", "Los Mochis 🌾"),
            Pair("Chinobabas", "Guasave ⚾"),
            Pair("Loca Simona", "Guamúchil 🐎"),
            Pair("Chuluy", "Navolato 🌾"),
            Pair("Chanito", "El Fuerte 🏞️"),
            Pair("Ese wey", "Badiraguato 🌲"),
            Pair("Compadre Chuy", "Mocorito 🎺")
        )

        return vaqueros.mapIndexed { index, pair ->
            GlobalLeaderboardEntry(
                rank = index + 1,
                playerName = pair.first,
                location = pair.second,
                timeSeconds = baseTimes[index],
                difficulty = difficulty.displayName,
                avatarEmoji = if (index == 0) "👑" else if (index < 3) "🥇" else "🤠"
            )
        }
    }
}
