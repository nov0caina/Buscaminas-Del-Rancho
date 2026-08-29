package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GameScoreEntity
import com.example.data.local.SavedGameEntity
import com.example.data.model.CellState
import com.example.data.model.ClickMode
import com.example.data.model.GameDifficulty
import com.example.data.model.GameStatus
import com.example.data.model.RanchFlagIcon
import com.example.data.model.RevealCluster
import com.example.data.model.VaqueroFace
import com.example.data.repository.GameRepository
import com.example.notification.DailyReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class GameUiState(
    val difficulty: GameDifficulty = GameDifficulty.PRINCIPIANTE,
    val rows: Int = 9,
    val cols: Int = 9,
    val mines: Int = 10,
    val grid: List<CellState> = emptyList(),
    val gameStatus: GameStatus = GameStatus.IDLE,
    val clickMode: ClickMode = ClickMode.REVEAL,
    val timeElapsed: Int = 0,
    val flagsPlaced: Int = 0,
    val vaqueroFace: VaqueroFace = VaqueroFace.HAPPY,
    val isDarkTheme: Boolean = false,
    val isHapticsEnabled: Boolean = true,
    val isDailyNotificationEnabled: Boolean = true,
    val hasSavedGame: Boolean = false,
    val ranchFlagIcon: RanchFlagIcon = RanchFlagIcon.SOMBRERO,
    val activeRevealCluster: RevealCluster? = null,
    val detonatedCell: Pair<Int, Int>? = null,
    val explosionEventId: Long = 0L,
    val victoryEventId: Long = 0L
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val vibrator = application.getSystemService(Vibrator::class.java)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _achievementUnlockEvents = MutableSharedFlow<com.example.data.local.AchievementEntity>(extraBufferCapacity = 6)
    val achievementUnlockEvents = _achievementUnlockEvents.asSharedFlow()

    private var timerJob: Job? = null

    val topScores: StateFlow<List<GameScoreEntity>>
    val allAchievements = MutableStateFlow<List<com.example.data.local.AchievementEntity>>(emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.initDefaultAchievementsIfNeeded()
            repository.allAchievements.collect { achievements ->
                allAchievements.value = achievements
            }
        }

        checkSavedGameAvailable()
        resetUiStateToDefaultGrid(GameDifficulty.PRINCIPIANTE)
    }

    private fun resetUiStateToDefaultGrid(difficulty: GameDifficulty) {
        val rows = difficulty.rows
        val cols = difficulty.cols
        val mines = difficulty.mines
        val emptyGrid = List(rows * cols) { index ->
            CellState(row = index / cols, col = index % cols)
        }

        _uiState.value = _uiState.value.copy(
            difficulty = difficulty,
            rows = rows,
            cols = cols,
            mines = mines,
            grid = emptyGrid,
            gameStatus = GameStatus.IDLE,
            timeElapsed = 0,
            flagsPlaced = 0,
            vaqueroFace = VaqueroFace.HAPPY,
            activeRevealCluster = null,
            detonatedCell = null,
            explosionEventId = 0L,
            victoryEventId = 0L
        )
    }

    fun startNewGame(
        difficulty: GameDifficulty,
        customRows: Int? = null,
        customCols: Int? = null,
        customMines: Int? = null
    ) {
        timerJob?.cancel()

        val defaultR = if (_uiState.value.difficulty == GameDifficulty.PERSONALIZADA) _uiState.value.rows else 10
        val defaultC = if (_uiState.value.difficulty == GameDifficulty.PERSONALIZADA) _uiState.value.cols else 10
        val defaultM = if (_uiState.value.difficulty == GameDifficulty.PERSONALIZADA) _uiState.value.mines else 15

        val targetCustomRows = customRows ?: defaultR
        val targetCustomCols = customCols ?: defaultC
        val targetCustomMines = customMines ?: defaultM

        val rows = if (difficulty == GameDifficulty.PERSONALIZADA) targetCustomRows.coerceIn(5, 30) else difficulty.rows
        val cols = if (difficulty == GameDifficulty.PERSONALIZADA) targetCustomCols.coerceIn(5, 20) else difficulty.cols
        val mines = if (difficulty == GameDifficulty.PERSONALIZADA) {
            targetCustomMines.coerceIn(1, (rows * cols * 0.8).toInt().coerceAtLeast(1))
        } else {
            difficulty.mines
        }

        val emptyGrid = List(rows * cols) { index ->
            CellState(row = index / cols, col = index % cols)
        }

        _uiState.value = _uiState.value.copy(
            difficulty = difficulty,
            rows = rows,
            cols = cols,
            mines = mines,
            grid = emptyGrid,
            gameStatus = GameStatus.IDLE,
            timeElapsed = 0,
            flagsPlaced = 0,
            vaqueroFace = VaqueroFace.HAPPY,
            activeRevealCluster = null,
            detonatedCell = null,
            explosionEventId = 0L,
            victoryEventId = 0L
        )

        viewModelScope.launch {
            repository.clearSavedGame()
            checkSavedGameAvailable()
        }
    }

    fun setClickMode(mode: ClickMode) {
        _uiState.value = _uiState.value.copy(clickMode = mode)
    }

    fun toggleClickMode() {
        val newMode = if (_uiState.value.clickMode == ClickMode.REVEAL) ClickMode.FLAG else ClickMode.REVEAL
        _uiState.value = _uiState.value.copy(clickMode = newMode)
    }

    fun setDarkTheme(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkTheme = enabled)
    }

    fun setHaptics(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isHapticsEnabled = enabled)
    }

    fun setDailyNotification(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDailyNotificationEnabled = enabled)
        if (enabled) {
            DailyReminderScheduler.scheduleDailyReminder(getApplication())
        } else {
            DailyReminderScheduler.cancelDailyReminder(getApplication())
        }
    }

    fun setRanchFlagIcon(icon: RanchFlagIcon) {
        _uiState.value = _uiState.value.copy(ranchFlagIcon = icon)
    }

    private fun triggerVibration(patternType: String) {
        if (!_uiState.value.isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            when (patternType) {
                "click" -> vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                "flag" -> vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                "explode" -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 200), -1))
                "win" -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 100, 50, 150), -1))
                "achievement" -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 100), -1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onCellClick(row: Int, col: Int) {
        val state = _uiState.value
        if (state.gameStatus == GameStatus.WON || state.gameStatus == GameStatus.LOST) return

        val index = row * state.cols + col
        if (index !in state.grid.indices) return
        val cell = state.grid[index]

        if (cell.isFlagged || cell.isRevealed) return

        if (state.clickMode == ClickMode.FLAG) {
            onCellLongClick(row, col)
            return
        }

        if (state.gameStatus == GameStatus.IDLE) {
            generateMinesAndStart(firstRow = row, firstCol = col)
            triggerVibration("click")
            revealCell(row, col)
            return
        }

        triggerVibration("click")
        revealCell(row, col)
    }

    fun onCellLongClick(row: Int, col: Int) {
        val state = _uiState.value
        if (state.gameStatus == GameStatus.WON || state.gameStatus == GameStatus.LOST) return

        val index = row * state.cols + col
        if (index !in state.grid.indices) return
        val cell = state.grid[index]

        if (cell.isRevealed) return

        triggerVibration("flag")

        val newGrid = state.grid.toMutableList()
        val newFlagged = !cell.isFlagged
        newGrid[index] = cell.copy(isFlagged = newFlagged)

        val newFlagsCount = state.flagsPlaced + (if (newFlagged) 1 else -1)
        _uiState.value = state.copy(grid = newGrid, flagsPlaced = newFlagsCount)

        autoSaveActiveGame()
    }

    fun onCellChord(row: Int, col: Int) {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.PLAYING) return

        val index = row * state.cols + col
        val cell = state.grid[index]
        if (!cell.isRevealed || cell.adjacentMines == 0) return

        val neighbors = getNeighbors(row, col, state.rows, state.cols)
        val flaggedNeighbors = neighbors.count { (r, c) -> state.grid[r * state.cols + c].isFlagged }

        if (flaggedNeighbors == cell.adjacentMines) {
            triggerVibration("click")
            var hitMine = false
            var detonatedR = -1
            var detonatedC = -1
            val newlyRevealedAll = mutableListOf<Pair<Int, Int>>()
            val grid = state.grid.toMutableList()

            for ((r, c) in neighbors) {
                val nIndex = r * state.cols + c
                val nCell = grid[nIndex]
                if (!nCell.isFlagged && !nCell.isRevealed) {
                    if (nCell.isMine) {
                        hitMine = true
                        detonatedR = r
                        detonatedC = c
                    } else {
                        val revealed = revealCellInternalOnGrid(grid, r, c, state.rows, state.cols)
                        newlyRevealedAll.addAll(revealed)
                    }
                }
            }

            if (hitMine) {
                gameOverLoss(detonatedRow = detonatedR, detonatedCol = detonatedC)
            } else {
                if (newlyRevealedAll.isNotEmpty()) {
                    val minR = newlyRevealedAll.minOf { it.first }
                    val maxR = newlyRevealedAll.maxOf { it.first }
                    val minC = newlyRevealedAll.minOf { it.second }
                    val maxC = newlyRevealedAll.maxOf { it.second }
                    val centerR = newlyRevealedAll.map { it.first }.average().toFloat()
                    val centerC = newlyRevealedAll.map { it.second }.average().toFloat()
                    val cluster = RevealCluster(
                        id = System.currentTimeMillis() + Random.nextLong(1000),
                        originRow = row,
                        originCol = col,
                        minRow = minR,
                        maxRow = maxR,
                        minCol = minC,
                        maxCol = maxC,
                        centerRow = centerR,
                        centerCol = centerC,
                        cellCount = newlyRevealedAll.size
                    )
                    _uiState.value = _uiState.value.copy(
                        grid = grid,
                        activeRevealCluster = cluster
                    )
                } else {
                    _uiState.value = _uiState.value.copy(grid = grid)
                }
                checkWinCondition()
            }
        }
    }

    private fun generateMinesAndStart(firstRow: Int, firstCol: Int) {
        val state = _uiState.value
        val totalCells = state.rows * state.cols
        val firstIndex = firstRow * state.cols + firstCol

        // Build list of safe index candidates around first click
        val safeIndices = mutableSetOf(firstIndex)
        getNeighbors(firstRow, firstCol, state.rows, state.cols).forEach { (r, c) ->
            safeIndices.add(r * state.cols + c)
        }

        val availableIndices = (0 until totalCells).filter { it !in safeIndices }.shuffled()
        val mineIndices = availableIndices.take(state.mines).toSet()

        val mutableGrid = ArrayList<CellState>(totalCells)

        for (i in 0 until totalCells) {
            val r = i / state.cols
            val c = i % state.cols
            mutableGrid.add(
                CellState(
                    row = r,
                    col = c,
                    isMine = i in mineIndices
                )
            )
        }

        // Calculate adjacent mines
        for (i in 0 until totalCells) {
            if (!mutableGrid[i].isMine) {
                val r = i / state.cols
                val c = i % state.cols
                val neighbors = getNeighbors(r, c, state.rows, state.cols)
                val count = neighbors.count { (nr, nc) -> mutableGrid[nr * state.cols + nc].isMine }
                mutableGrid[i] = mutableGrid[i].copy(adjacentMines = count)
            }
        }

        _uiState.value = _uiState.value.copy(
            grid = mutableGrid,
            gameStatus = GameStatus.PLAYING,
            vaqueroFace = VaqueroFace.HAPPY
        )

        autoSaveActiveGame()
        startTimer()
    }

    private fun revealCell(row: Int, col: Int) {
        val state = _uiState.value
        val index = row * state.cols + col
        val cell = state.grid[index]

        if (cell.isMine) {
            gameOverLoss(detonatedRow = row, detonatedCol = col)
            return
        }

        val grid = state.grid.toMutableList()
        val newlyRevealed = revealCellInternalOnGrid(grid, row, col, state.rows, state.cols)

        if (newlyRevealed.isNotEmpty()) {
            val minR = newlyRevealed.minOf { it.first }
            val maxR = newlyRevealed.maxOf { it.first }
            val minC = newlyRevealed.minOf { it.second }
            val maxC = newlyRevealed.maxOf { it.second }
            val centerR = newlyRevealed.map { it.first }.average().toFloat()
            val centerC = newlyRevealed.map { it.second }.average().toFloat()
            val cluster = RevealCluster(
                id = System.currentTimeMillis() + Random.nextLong(1000),
                originRow = row,
                originCol = col,
                minRow = minR,
                maxRow = maxR,
                minCol = minC,
                maxCol = maxC,
                centerRow = centerR,
                centerCol = centerC,
                cellCount = newlyRevealed.size
            )
            _uiState.value = _uiState.value.copy(
                grid = grid,
                activeRevealCluster = cluster
            )
        } else {
            _uiState.value = _uiState.value.copy(grid = grid)
        }

        checkWinCondition()
    }

    private fun revealCellInternalOnGrid(
        grid: MutableList<CellState>,
        startRow: Int,
        startCol: Int,
        rows: Int,
        cols: Int
    ): List<Pair<Int, Int>> {
        val newlyRevealed = mutableListOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(startRow to startCol)

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            val idx = r * cols + c
            val current = grid[idx]

            if (current.isRevealed || current.isFlagged) continue

            grid[idx] = current.copy(isRevealed = true)
            newlyRevealed.add(r to c)

            if (current.adjacentMines == 0 && !current.isMine) {
                for ((nr, nc) in getNeighbors(r, c, rows, cols)) {
                    val nIdx = nr * cols + nc
                    if (!grid[nIdx].isRevealed && !grid[nIdx].isFlagged) {
                        queue.add(nr to nc)
                    }
                }
            }
        }

        return newlyRevealed
    }

    private fun checkWinCondition() {
        val state = _uiState.value
        val unrevealedCount = state.grid.count { !it.isRevealed }

        if (unrevealedCount == state.mines) {
            gameOverWin()
        } else {
            autoSaveActiveGame()
        }
    }

    private fun gameOverLoss(detonatedRow: Int, detonatedCol: Int) {
        timerJob?.cancel()
        triggerVibration("explode")

        val state = _uiState.value
        val newGrid = state.grid.map { cell ->
            when {
                cell.row == detonatedRow && cell.col == detonatedCol -> cell.copy(isRevealed = true, isExploded = true)
                cell.isMine -> cell.copy(isRevealed = true)
                else -> cell
            }
        }

        val eventId = System.currentTimeMillis() + Random.nextLong(1000)

        _uiState.value = state.copy(
            grid = newGrid,
            gameStatus = GameStatus.LOST,
            vaqueroFace = VaqueroFace.DEAD,
            detonatedCell = Pair(detonatedRow, detonatedCol),
            explosionEventId = eventId
        )

        viewModelScope.launch {
            repository.recordGameFinished(
                difficulty = state.difficulty,
                timeSeconds = state.timeElapsed,
                isWin = false,
                rows = state.rows,
                cols = state.cols,
                mines = state.mines,
                flagsPlaced = state.flagsPlaced
            )
            repository.clearSavedGame()
            checkSavedGameAvailable()
        }
    }

    private fun gameOverWin() {
        timerJob?.cancel()
        triggerVibration("win")

        val state = _uiState.value
        val newGrid = state.grid.map { cell ->
            if (cell.isMine) cell.copy(isFlagged = true) else cell
        }

        val victoryId = System.currentTimeMillis() + Random.nextLong(1000)

        _uiState.value = state.copy(
            grid = newGrid,
            gameStatus = GameStatus.WON,
            flagsPlaced = state.mines,
            vaqueroFace = VaqueroFace.VICTORIOUS,
            victoryEventId = victoryId
        )

        viewModelScope.launch {
            val newlyUnlocked = repository.recordGameFinished(
                difficulty = state.difficulty,
                timeSeconds = state.timeElapsed,
                isWin = true,
                rows = state.rows,
                cols = state.cols,
                mines = state.mines,
                flagsPlaced = state.flagsPlaced
            )
            if (newlyUnlocked.isNotEmpty()) {
                triggerVibration("achievement")
                newlyUnlocked.forEach { achievement ->
                    _achievementUnlockEvents.emit(achievement)
                }
            }
            repository.clearSavedGame()
            checkSavedGameAvailable()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.gameStatus == GameStatus.PLAYING) {
                delay(1000)
                _uiState.value = _uiState.value.copy(timeElapsed = _uiState.value.timeElapsed + 1)
                if (_uiState.value.timeElapsed % 3 == 0) {
                    autoSaveActiveGame()
                }
            }
        }
    }

    private fun getNeighbors(r: Int, c: Int, rows: Int, cols: Int): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    list.add(nr to nc)
                }
            }
        }
        return list
    }

    fun autoSaveActiveGame() {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.PLAYING) return

        viewModelScope.launch {
            val jsonArray = JSONArray()
            state.grid.forEach { cell ->
                val obj = JSONObject().apply {
                    put("r", cell.row)
                    put("c", cell.col)
                    put("m", cell.isMine)
                    put("rev", cell.isRevealed)
                    put("flg", cell.isFlagged)
                    put("adj", cell.adjacentMines)
                }
                jsonArray.put(obj)
            }

            repository.saveActiveGame(
                difficultyName = state.difficulty.name,
                rows = state.rows,
                cols = state.cols,
                mines = state.mines,
                timeSeconds = state.timeElapsed,
                boardJson = jsonArray.toString(),
                gameStatus = state.gameStatus.name
            )
            checkSavedGameAvailable()
        }
    }

    fun checkSavedGameAvailable() {
        viewModelScope.launch {
            val saved = repository.getSavedGame()
            _uiState.value = _uiState.value.copy(hasSavedGame = saved != null)
        }
    }

    fun resumeSavedGame() {
        viewModelScope.launch {
            val saved = repository.getSavedGame() ?: return@launch

            val diff = try {
                GameDifficulty.valueOf(saved.difficultyName)
            } catch (e: Exception) {
                GameDifficulty.PRINCIPIANTE
            }

            val jsonArray = JSONArray(saved.boardJson)
            val restoredGrid = mutableListOf<CellState>()
            var flags = 0

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val isFlg = obj.optBoolean("flg", false)
                if (isFlg) flags++

                restoredGrid.add(
                    CellState(
                        row = obj.getInt("r"),
                        col = obj.getInt("c"),
                        isMine = obj.getBoolean("m"),
                        isRevealed = obj.getBoolean("rev"),
                        isFlagged = isFlg,
                        adjacentMines = obj.getInt("adj")
                    )
                )
            }

            _uiState.value = _uiState.value.copy(
                difficulty = diff,
                rows = saved.rows,
                cols = saved.cols,
                mines = saved.mines,
                grid = restoredGrid,
                gameStatus = GameStatus.PLAYING,
                timeElapsed = saved.timeElapsedSeconds,
                flagsPlaced = flags,
                vaqueroFace = VaqueroFace.HAPPY
            )

            startTimer()
        }
    }

    fun getGlobalLeaderboard(): List<com.example.data.repository.GlobalLeaderboardEntry> {
        return repository.getGlobalSinaloaLeaderboard(_uiState.value.difficulty)
    }
}
