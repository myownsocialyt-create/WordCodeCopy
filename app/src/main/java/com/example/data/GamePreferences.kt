package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)

    private val _coins = MutableStateFlow(loadCoins())
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _completedLevels = MutableStateFlow(loadCompletedLevels())
    val completedLevels: StateFlow<Set<String>> = _completedLevels.asStateFlow()

    private val _unlockedLevels = MutableStateFlow(loadUnlockedLevels())
    val unlockedLevels: StateFlow<Set<String>> = _unlockedLevels.asStateFlow()

    private fun loadCoins(): Int {
        return prefs.getInt(KEY_COINS, 300)
    }

    private fun loadCompletedLevels(): Set<String> {
        return prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
    }

    private fun loadUnlockedLevels(): Set<String> {
        val saved = prefs.getStringSet(KEY_UNLOCKED_LEVELS, emptySet()) ?: emptySet()
        val firstId = CategoryRepository.CATEGORIES.firstOrNull()?.id
        return if (firstId != null && !saved.contains(firstId)) {
            saved + firstId
        } else {
            saved
        }
    }

    fun addCoins(amount: Int) {
        val newBalance = _coins.value + amount
        _coins.value = newBalance
        prefs.edit().putInt(KEY_COINS, newBalance).apply()
    }

    fun spendCoins(amount: Int): Boolean {
        if (_coins.value >= amount) {
            val newBalance = _coins.value - amount
            _coins.value = newBalance
            prefs.edit().putInt(KEY_COINS, newBalance).apply()
            return true
        }
        return false
    }

    /**
     * Completing a level:
     * 1. Marks this level as completed.
     * 2. Automatically unlocks the NEXT previously-locked level in the sequence.
     */
    fun markLevelCompleted(categoryId: String) {
        val updatedCompleted = _completedLevels.value.toMutableSet()
        updatedCompleted.add(categoryId)
        _completedLevels.value = updatedCompleted
        prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, updatedCompleted).apply()

        // Unlock next previously locked level
        unlockNextLockedLevel(completedCategoryId = categoryId)
    }

    /**
     * Directly unlocks a specific category (e.g. bought with 300 coins).
     */
    fun unlockLevel(categoryId: String) {
        val updated = _unlockedLevels.value.toMutableSet()
        updated.add(categoryId)
        _unlockedLevels.value = updated
        prefs.edit().putStringSet(KEY_UNLOCKED_LEVELS, updated).apply()
    }

    /**
     * Unlocks the next locked level. If the played level was bought out of order
     * (e.g., Level 7 played while Level 2 was already unlocked), it unlocks Level 3 (the next locked level in sequence).
     */
    fun unlockNextLockedLevel(completedCategoryId: String? = null) {
        val currentUnlocked = _unlockedLevels.value
        // First check if immediate next category after completed category is locked
        var targetToUnlock: String? = null
        if (completedCategoryId != null) {
            val completedIndex = CategoryRepository.CATEGORIES.indexOfFirst { it.id == completedCategoryId }
            if (completedIndex != -1 && completedIndex + 1 < CategoryRepository.CATEGORIES.size) {
                val nextCandidate = CategoryRepository.CATEGORIES[completedIndex + 1].id
                if (!currentUnlocked.contains(nextCandidate)) {
                    targetToUnlock = nextCandidate
                }
            }
        }

        // If nextCandidate was already unlocked or no next candidate, find the earliest locked level in sequence
        if (targetToUnlock == null) {
            for (cat in CategoryRepository.CATEGORIES) {
                if (!currentUnlocked.contains(cat.id)) {
                    targetToUnlock = cat.id
                    break
                }
            }
        }

        if (targetToUnlock != null) {
            unlockLevel(targetToUnlock)
        }
    }

    fun isCategoryUnlocked(categoryId: String): Boolean {
        val firstId = CategoryRepository.CATEGORIES.firstOrNull()?.id
        if (categoryId == firstId) return true
        return _unlockedLevels.value.contains(categoryId)
    }

    fun isCategoryUnlocked(categoryIndex: Int): Boolean {
        val cat = CategoryRepository.CATEGORIES.getOrNull(categoryIndex) ?: return false
        return isCategoryUnlocked(cat.id)
    }

    fun saveHighScore(categoryId: String, score: Int) {
        val current = getHighScore(categoryId)
        if (score > current) {
            prefs.edit().putInt("high_score_$categoryId", score).apply()
        }
    }

    fun getHighScore(categoryId: String): Int {
        return prefs.getInt("high_score_$categoryId", 0)
    }

    companion object {
        private const val KEY_COINS = "user_coins"
        private const val KEY_COMPLETED_LEVELS = "completed_levels"
        private const val KEY_UNLOCKED_LEVELS = "unlocked_levels"

        @Volatile
        private var INSTANCE: GamePreferences? = null

        fun getInstance(context: Context): GamePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GamePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
