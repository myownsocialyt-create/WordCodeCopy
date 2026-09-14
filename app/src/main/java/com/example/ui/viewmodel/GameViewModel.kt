package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.data.BonusDictionary
import com.example.data.Category
import com.example.data.CategoryRepository
import com.example.data.GamePreferences
import com.example.engine.SoundManager
import com.example.engine.WordSearchGenerator
import com.example.model.CellCoord
import com.example.model.FoundWord
import com.example.model.GameMode
import com.example.model.PlacedWord
import com.example.model.WordGrid
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

data class GameUiState(
    val category: Category? = null,
    val mode: GameMode = GameMode.CLASSIC,
    val grid: WordGrid? = null,
    val foundWords: List<FoundWord> = emptyList(),
    val bonusWordsFound: List<String> = emptyList(),
    val score: Int = 0,
    val hintsRemaining: Int = 3,
    val elapsedTimeSeconds: Int = 0,
    val timeRemainingSeconds: Int = 120,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val bonusTimeScore: Int = 0,
    val coinsEarned: Int = 0,
    val selectedPath: List<CellCoord> = emptyList(),
    val hintFlashCells: Set<CellCoord> = emptySet(),
    val hintFlashActive: Boolean = false,
    val latestBonusWord: String? = null
)

sealed class GameEvent {
    data class NavigateToResults(val isVictory: Boolean) : GameEvent()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = GamePreferences.getInstance(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<GameEvent>()
    val eventFlow: SharedFlow<GameEvent> = _eventFlow.asSharedFlow()

    val userCoins = prefs.coins
    val completedLevels = prefs.completedLevels
    val unlockedLevels = prefs.unlockedLevels

    /**
     * Attempts to unlock a category for 300 coins.
     * Returns true if successful, false if insufficient coins.
     */
    fun unlockCategoryWithCoins(categoryId: String): Boolean {
        if (prefs.spendCoins(300)) {
            prefs.unlockLevel(categoryId)
            SoundManager.play(SoundManager.SoundType.FANFARE)
            return true
        }
        return false
    }

    private var timerJob: Job? = null
    private var hintAnimationJob: Job? = null
    private var bonusWordToastJob: Job? = null

    fun startNewGame(categoryId: String, mode: GameMode) {
        val cat = CategoryRepository.getCategoryById(categoryId) ?: CategoryRepository.CATEGORIES.first()
        val generatedGrid = WordSearchGenerator.generateGrid(cat.words)

        _uiState.value = GameUiState(
            category = cat,
            mode = mode,
            grid = generatedGrid,
            foundWords = emptyList(),
            bonusWordsFound = emptyList(),
            score = 0,
            hintsRemaining = 3,
            elapsedTimeSeconds = 0,
            timeRemainingSeconds = 120,
            isGameOver = false,
            isVictory = false,
            bonusTimeScore = 0,
            coinsEarned = 0,
            selectedPath = emptyList(),
            hintFlashCells = emptySet(),
            hintFlashActive = false
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_uiState.value.isGameOver) {
                delay(1000L)
                val current = _uiState.value
                if (current.mode == GameMode.CLASSIC) {
                    _uiState.value = current.copy(
                        elapsedTimeSeconds = current.elapsedTimeSeconds + 1
                    )
                } else {
                    // Time Mode countdown
                    val remaining = current.timeRemainingSeconds - 1
                    if (remaining in 1..29) {
                        SoundManager.play(SoundManager.SoundType.TICK)
                    }
                    if (remaining <= 0) {
                        // Time out! Game Over
                        _uiState.value = current.copy(
                            timeRemainingSeconds = 0,
                            isGameOver = true,
                            isVictory = false
                        )
                        SoundManager.play(SoundManager.SoundType.GAME_OVER)
                        _eventFlow.emit(GameEvent.NavigateToResults(isVictory = false))
                        break
                    } else {
                        _uiState.value = current.copy(
                            timeRemainingSeconds = remaining,
                            elapsedTimeSeconds = current.elapsedTimeSeconds + 1
                        )
                    }
                }
            }
        }
    }

    /**
     * Touch drag interaction: update straight-line selection from start cell to current drag cell
     */
    fun onSelectionDrag(start: CellCoord, current: CellCoord) {
        val grid = _uiState.value.grid ?: return
        val path = calculateStraightLine(start, current, grid.size)
        if (path != _uiState.value.selectedPath) {
            if (path.size > _uiState.value.selectedPath.size) {
                SoundManager.play(SoundManager.SoundType.SWOOSH)
            }
            _uiState.value = _uiState.value.copy(selectedPath = path)
        }
    }

    fun onSelectionEnd() {
        val path = _uiState.value.selectedPath
        val grid = _uiState.value.grid ?: return

        if (path.size < 2) {
            _uiState.value = _uiState.value.copy(selectedPath = emptyList())
            return
        }

        // Extract word from path letters
        val letters = StringBuilder()
        for (coord in path) {
            letters.append(grid.letters[coord.row][coord.col])
        }
        val candidateForward = letters.toString()
        val candidateReverse = candidateForward.reversed()

        val foundWordsList = _uiState.value.foundWords
        val remainingWords = grid.words.filter { word ->
            !foundWordsList.any { it.word.equals(word.word, ignoreCase = true) }
        }

        // Check if candidate matches any unfound word
        val matchedWord = remainingWords.find {
            it.word.equals(candidateForward, ignoreCase = true) ||
                    it.word.equals(candidateReverse, ignoreCase = true)
        }

        if (matchedWord != null) {
            // Found a puzzle word!
            val newFoundWord = FoundWord(
                word = matchedWord.word,
                colorIndex = matchedWord.colorIndex,
                path = matchedWord.path
            )
            val updatedFound = _uiState.value.foundWords + newFoundWord
            val updatedScore = _uiState.value.score + 10

            SoundManager.play(SoundManager.SoundType.CHIME)

            val isVictory = updatedFound.size >= grid.words.size

            if (isVictory) {
                timerJob?.cancel()
                val timeBonus = if (_uiState.value.mode == GameMode.TIME) {
                    _uiState.value.timeRemainingSeconds * 2
                } else 0

                val totalScore = updatedScore + timeBonus
                val coinsEarned = 50

                prefs.addCoins(coinsEarned)
                _uiState.value.category?.let {
                    prefs.markLevelCompleted(it.id)
                    prefs.saveHighScore(it.id, totalScore)
                }

                _uiState.value = _uiState.value.copy(
                    foundWords = updatedFound,
                    score = totalScore,
                    bonusTimeScore = timeBonus,
                    coinsEarned = coinsEarned,
                    isGameOver = true,
                    isVictory = true,
                    selectedPath = emptyList()
                )

                SoundManager.play(SoundManager.SoundType.FANFARE)

                viewModelScope.launch {
                    delay(400L)
                    _eventFlow.emit(GameEvent.NavigateToResults(isVictory = true))
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    foundWords = updatedFound,
                    score = updatedScore,
                    selectedPath = emptyList()
                )
            }
        } else {
            // Check for bonus word
            val puzzleWordList = grid.words.map { it.word }
            val isBonusFwd = BonusDictionary.isBonusWord(candidateForward, puzzleWordList)
            val isBonusRev = BonusDictionary.isBonusWord(candidateReverse, puzzleWordList)

            val bonusWord = when {
                isBonusFwd && !_uiState.value.bonusWordsFound.contains(candidateForward) -> candidateForward
                isBonusRev && !_uiState.value.bonusWordsFound.contains(candidateReverse) -> candidateReverse
                else -> null
            }

            if (bonusWord != null) {
                // Award +5 points for bonus word
                SoundManager.play(SoundManager.SoundType.SPARKLE)
                val updatedBonus = _uiState.value.bonusWordsFound + bonusWord
                val updatedScore = _uiState.value.score + 5

                _uiState.value = _uiState.value.copy(
                    bonusWordsFound = updatedBonus,
                    score = updatedScore,
                    selectedPath = emptyList(),
                    latestBonusWord = bonusWord
                )

                bonusWordToastJob?.cancel()
                bonusWordToastJob = viewModelScope.launch {
                    delay(2000L)
                    _uiState.value = _uiState.value.copy(latestBonusWord = null)
                }
            } else {
                // Wrong selection
                SoundManager.play(SoundManager.SoundType.BUZZ)
                _uiState.value = _uiState.value.copy(selectedPath = emptyList())
            }
        }
    }

    /**
     * Hint system:
     * 3 hints per puzzle.
     * Picks a random unfound word and flashes cells in yellow (3 pulses over 1.5s).
     */
    fun useHint() {
        val current = _uiState.value
        if (current.hintsRemaining <= 0) return

        val grid = current.grid ?: return
        val unfoundWords = grid.words.filter { word ->
            !current.foundWords.any { it.word.equals(word.word, ignoreCase = true) }
        }

        if (unfoundWords.isEmpty()) return

        val targetWord = unfoundWords.random()
        val flashCells = targetWord.path.toSet()

        _uiState.value = current.copy(
            hintsRemaining = current.hintsRemaining - 1,
            hintFlashCells = flashCells,
            hintFlashActive = true
        )
        SoundManager.play(SoundManager.SoundType.TAP)

        hintAnimationJob?.cancel()
        hintAnimationJob = viewModelScope.launch {
            // 3 pulses over 1.5 seconds (each cycle 500ms: 250ms on, 250ms off)
            repeat(3) {
                _uiState.value = _uiState.value.copy(hintFlashActive = true)
                delay(250L)
                _uiState.value = _uiState.value.copy(hintFlashActive = false)
                delay(250L)
            }
            _uiState.value = _uiState.value.copy(
                hintFlashCells = emptySet(),
                hintFlashActive = false
            )
        }
    }

    /**
     * Rewarded ad extra hints:
     * Watch ad to get +2 free hints.
     * Reward is ONLY granted if user completes watching the full video.
     */
    fun watchAdForHints(activity: Activity) {
        AdManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                _uiState.value = _uiState.value.copy(
                    hintsRemaining = _uiState.value.hintsRemaining + 2
                )
                SoundManager.play(SoundManager.SoundType.SPARKLE)
                Toast.makeText(activity, "+2 Free Hints unlocked!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Rewarded ad free coins:
     * Watch ad to get +50 free coins.
     * Reward is ONLY granted if user completes watching the full video.
     */
    fun watchAdForCoins(activity: Activity) {
        AdManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                prefs.addCoins(50)
                SoundManager.play(SoundManager.SoundType.SPARKLE)
                Toast.makeText(activity, "+50 Free Coins added!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun buyHintWithCoins() {
        if (prefs.spendCoins(30)) {
            _uiState.value = _uiState.value.copy(
                hintsRemaining = _uiState.value.hintsRemaining + 1
            )
            SoundManager.play(SoundManager.SoundType.SPARKLE)
        }
    }

    /**
     * Calculates straight line points between start and target cell
     * Constrained to 8 directions (horizontal, vertical, diagonals)
     */
    private fun calculateStraightLine(
        start: CellCoord,
        target: CellCoord,
        gridSize: Int
    ): List<CellCoord> {
        val dRow = target.row - start.row
        val dCol = target.col - start.col

        if (dRow == 0 && dCol == 0) {
            return listOf(start)
        }

        val absRow = abs(dRow)
        val absCol = abs(dCol)

        // Determine dominant direction:
        // Horizontal: absRow is much smaller than absCol
        // Vertical: absCol is much smaller than absRow
        // Diagonal: roughly equal (absRow / absCol in [0.5, 2.0])
        val stepR: Int
        val stepC: Int
        val steps: Int

        when {
            absRow == 0 -> {
                stepR = 0
                stepC = if (dCol > 0) 1 else -1
                steps = absCol
            }
            absCol == 0 -> {
                stepR = if (dRow > 0) 1 else -1
                stepC = 0
                steps = absRow
            }
            absRow == absCol -> {
                stepR = if (dRow > 0) 1 else -1
                stepC = if (dCol > 0) 1 else -1
                steps = absRow
            }
            absRow > 2 * absCol -> {
                // Snap to vertical
                stepR = if (dRow > 0) 1 else -1
                stepC = 0
                steps = absRow
            }
            absCol > 2 * absRow -> {
                // Snap to horizontal
                stepR = 0
                stepC = if (dCol > 0) 1 else -1
                steps = absCol
            }
            else -> {
                // Snap to closest diagonal
                stepR = if (dRow > 0) 1 else -1
                stepC = if (dCol > 0) 1 else -1
                steps = max(absRow, absCol)
            }
        }

        val result = mutableListOf<CellCoord>()
        for (i in 0..steps) {
            val r = start.row + i * stepR
            val c = start.col + i * stepC
            if (r in 0 until gridSize && c in 0 until gridSize) {
                result.add(CellCoord(r, c))
            } else {
                break
            }
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        hintAnimationJob?.cancel()
        bonusWordToastJob?.cancel()
    }
}
