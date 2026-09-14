package com.example.model

enum class GameMode(val displayName: String, val description: String) {
    CLASSIC("Classic Mode", "Relax and take your time. Count up timer."),
    TIME("Time Mode", "2-minute countdown challenge with speed bonuses!")
}

data class CellCoord(
    val row: Int,
    val col: Int
)

data class PlacedWord(
    val word: String,
    val path: List<CellCoord>,
    val colorIndex: Int,
    val isFound: Boolean = false
)

data class FoundWord(
    val word: String,
    val colorIndex: Int,
    val path: List<CellCoord>
)

data class WordGrid(
    val size: Int,
    val letters: Array<CharArray>,
    val words: List<PlacedWord>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WordGrid) return false
        if (size != other.size) return false
        if (!letters.contentDeepEquals(other.letters)) return false
        if (words != other.words) return false
        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + letters.contentDeepHashCode()
        result = 31 * result + words.hashCode()
        return result
    }
}
