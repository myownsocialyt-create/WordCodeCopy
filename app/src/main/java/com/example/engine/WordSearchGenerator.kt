package com.example.engine

import com.example.model.CellCoord
import com.example.model.PlacedWord
import com.example.model.WordGrid
import kotlin.random.Random

object WordSearchGenerator {

    private val DIRECTIONS = listOf(
        Pair(0, 1),   // Right
        Pair(0, -1),  // Left
        Pair(1, 0),   // Down
        Pair(-1, 0),  // Up
        Pair(1, 1),   // Down-Right
        Pair(-1, -1), // Up-Left
        Pair(1, -1),  // Down-Left
        Pair(-1, 1)   // Up-Right
    )

    // Letter distribution weighted roughly to common English letters
    private val LETTER_POOL = "AAAAABBCCCCDDDEEEEEEEFFFGGGHHHHIIIIJKLLLLMMMNNNNNOOOOOOOPPQRRRRRSSSSSSTTTTTTUUUUVWWXYZ"

    fun calculateGridSize(words: List<String>): Int {
        val count = words.size
        val longest = words.maxOfOrNull { it.length } ?: 0
        return when {
            count >= 16 || longest >= 10 -> 12
            count >= 12 || longest >= 7 -> 10
            else -> 8
        }
    }

    fun generateGrid(words: List<String>, random: Random = Random): WordGrid {
        val initialSize = calculateGridSize(words)
        // Sort words by length descending to place harder (longer) words first
        val sortedWords = words.sortedByDescending { it.length }

        var bestGrid: WordGrid? = null
        var size = initialSize

        for (attempt in 0 until 60) {
            val gridLetters = Array(size) { CharArray(size) { ' ' } }
            val placedList = mutableListOf<PlacedWord>()
            var allPlaced = true

            for ((colorIdx, word) in sortedWords.withIndex()) {
                val cleanWord = word.trim().uppercase()
                val path = tryPlaceWord(gridLetters, cleanWord, size, random)
                if (path != null) {
                    placedList.add(
                        PlacedWord(
                            word = cleanWord,
                            path = path,
                            colorIndex = colorIdx % 18,
                            isFound = false
                        )
                    )
                } else {
                    allPlaced = false
                    break
                }
            }

            if (allPlaced) {
                // Fill remaining empty cells with random letters
                for (r in 0 until size) {
                    for (c in 0 until size) {
                        if (gridLetters[r][c] == ' ') {
                            gridLetters[r][c] = LETTER_POOL[random.nextInt(LETTER_POOL.length)]
                        }
                    }
                }
                // Keep original word order for display consistency
                val wordMap = placedList.associateBy { it.word }
                val orderedPlacedWords = words.mapNotNull { wordMap[it.trim().uppercase()] }
                bestGrid = WordGrid(size = size, letters = gridLetters, words = orderedPlacedWords)
                break
            }

            // If we struggled after many attempts, increment grid size slightly
            if (attempt == 40 && size < 14) {
                size++
            }
        }

        return bestGrid ?: run {
            // Ultimate fallback: generate with whatever was placed or expand size
            fallbackGrid(words, size.coerceAtLeast(initialSize), random)
        }
    }

    private fun tryPlaceWord(
        grid: Array<CharArray>,
        word: String,
        size: Int,
        random: Random
    ): List<CellCoord>? {
        val dirs = DIRECTIONS.shuffled(random)
        val wordLen = word.length

        // Try up to 150 random positions
        val coords = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                coords.add(Pair(r, c))
            }
        }
        coords.shuffle(random)

        for ((r, c) in coords) {
            for ((dr, dc) in dirs) {
                val endR = r + (wordLen - 1) * dr
                val endC = c + (wordLen - 1) * dc

                if (endR in 0 until size && endC in 0 until size) {
                    var fits = true
                    val path = ArrayList<CellCoord>(wordLen)

                    for (i in 0 until wordLen) {
                        val currR = r + i * dr
                        val currC = c + i * dc
                        val existing = grid[currR][currC]
                        if (existing != ' ' && existing != word[i]) {
                            fits = false
                            break
                        }
                        path.add(CellCoord(currR, currC))
                    }

                    if (fits) {
                        for (i in 0 until wordLen) {
                            grid[path[i].row][path[i].col] = word[i]
                        }
                        return path
                    }
                }
            }
        }

        return null
    }

    private fun fallbackGrid(words: List<String>, size: Int, random: Random): WordGrid {
        val s = (size + 1).coerceAtLeast(calculateGridSize(words))
        val gridLetters = Array(s) { CharArray(s) { ' ' } }
        val placedList = mutableListOf<PlacedWord>()

        for ((idx, word) in words.withIndex()) {
            val cleanWord = word.trim().uppercase()
            val path = tryPlaceWord(gridLetters, cleanWord, s, random) ?: run {
                // Simple horizontal placement if possible
                val r = idx % s
                val pathCoords = mutableListOf<CellCoord>()
                for (c in 0 until minOf(cleanWord.length, s)) {
                    gridLetters[r][c] = cleanWord[c]
                    pathCoords.add(CellCoord(r, c))
                }
                pathCoords
            }
            placedList.add(
                PlacedWord(
                    word = cleanWord,
                    path = path,
                    colorIndex = idx % 18,
                    isFound = false
                )
            )
        }

        for (r in 0 until s) {
            for (c in 0 until s) {
                if (gridLetters[r][c] == ' ') {
                    gridLetters[r][c] = LETTER_POOL[random.nextInt(LETTER_POOL.length)]
                }
            }
        }

        return WordGrid(size = s, letters = gridLetters, words = placedList)
    }
}
