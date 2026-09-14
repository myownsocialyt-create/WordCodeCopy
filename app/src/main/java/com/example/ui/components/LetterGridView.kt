package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CellCoord
import com.example.model.FoundWord
import com.example.model.WordGrid
import com.example.ui.theme.GameBlue
import com.example.ui.theme.GameCardBorder
import com.example.ui.theme.GameDarkText
import com.example.ui.theme.WordHighlightColors

@Composable
fun LetterGridView(
    grid: WordGrid,
    foundWords: List<FoundWord>,
    selectedPath: List<CellCoord>,
    hintFlashCells: Set<CellCoord>,
    hintFlashActive: Boolean,
    onDragStart: (CellCoord) -> Unit,
    onDrag: (CellCoord, CellCoord) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val size = grid.size
    var dragStartCell by remember { mutableStateOf<CellCoord?>(null) }
    var currentDragCell by remember { mutableStateOf<CellCoord?>(null) }

    // Map each cell coord to found word colors (at 35% opacity)
    val cellFoundColors = remember(foundWords) {
        val map = mutableMapOf<CellCoord, Color>()
        for (found in foundWords) {
            val color = WordHighlightColors.getOrElse(found.colorIndex % WordHighlightColors.size) {
                GameBlue
            }.copy(alpha = 0.35f)
            for (coord in found.path) {
                map[coord] = color
            }
        }
        map
    }

    val selectedSet = remember(selectedPath) { selectedPath.toSet() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .aspectRatio(1f)
            .testTag("word_search_grid_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val cellWidthPx = (widthPx / size).coerceAtLeast(1f)
            val cellHeightPx = (heightPx / size).coerceAtLeast(1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(size, cellWidthPx, cellHeightPx) {
                        detectDragGestures(
                            onDragStart = { offset: Offset ->
                                val c = (offset.x / cellWidthPx).toInt().coerceIn(0, size - 1)
                                val r = (offset.y / cellHeightPx).toInt().coerceIn(0, size - 1)
                                val start = CellCoord(r, c)
                                dragStartCell = start
                                currentDragCell = start
                                onDragStart(start)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val start = dragStartCell ?: return@detectDragGestures
                                val offset = change.position
                                val c = (offset.x / cellWidthPx).toInt().coerceIn(0, size - 1)
                                val r = (offset.y / cellHeightPx).toInt().coerceIn(0, size - 1)
                                val current = CellCoord(r, c)
                                if (current != currentDragCell) {
                                    currentDragCell = current
                                    onDrag(start, current)
                                }
                            },
                            onDragEnd = {
                                dragStartCell = null
                                currentDragCell = null
                                onDragEnd()
                            },
                            onDragCancel = {
                                dragStartCell = null
                                currentDragCell = null
                                onDragEnd()
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    for (r in 0 until size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (c in 0 until size) {
                                val coord = CellCoord(r, c)
                                val letter = grid.letters[r][c]

                                val isSelected = selectedSet.contains(coord)
                                val isHintActive = hintFlashActive && hintFlashCells.contains(coord)
                                val foundColor = cellFoundColors[coord]

                                val targetBgColor = when {
                                    isHintActive -> Color(0xFFFFD54F) // Yellow flash
                                    isSelected -> GameBlue.copy(alpha = 0.35f)
                                    foundColor != null -> foundColor
                                    else -> Color.Transparent
                                }

                                val animatedBgColor by animateColorAsState(
                                    targetValue = targetBgColor,
                                    animationSpec = tween(durationMillis = 180),
                                    label = "cell_bg"
                                )

                                val fontSize = when {
                                    size >= 12 -> 14.sp
                                    size >= 10 -> 17.sp
                                    else -> 21.sp
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(animatedBgColor)
                                        .then(
                                            if (isHintActive) {
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = Color(0xFFF59E0B),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                            } else if (isSelected) {
                                                Modifier.border(
                                                    width = 1.5.dp,
                                                    color = GameBlue,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        fontWeight = if (isSelected || foundColor != null || isHintActive)
                                            FontWeight.ExtraBold
                                        else FontWeight.SemiBold,
                                        fontSize = fontSize,
                                        color = when {
                                            isHintActive -> Color(0xFF78350F)
                                            isSelected -> GameBlue
                                            else -> GameDarkText
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

