package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.findActivity
import com.example.engine.SoundManager
import com.example.model.GameMode
import com.example.ui.components.FreeCoinsDialog
import com.example.ui.components.LetterGridView
import com.example.ui.theme.GameBackground
import com.example.ui.theme.GameBlue
import com.example.ui.theme.GameDarkText
import com.example.ui.theme.GameGrayedFoundWord
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GamePink
import com.example.ui.theme.GameRedTimer
import com.example.ui.theme.GameRedTimerBg
import com.example.ui.theme.GameSecondaryText
import com.example.ui.theme.WordHighlightColors
import com.example.ui.viewmodel.GameEvent
import com.example.ui.viewmodel.GameViewModel
import com.example.util.NetworkMonitor
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WifiOff

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToResults: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()
    val context = LocalContext.current

    var showExitDialog by remember { mutableStateOf(false) }
    var showExtraHintsDialog by remember { mutableStateOf(false) }
    var showFreeCoinsDialog by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(SoundManager.isMuted) }

    // Intercept phone's physical or gesture back button
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is GameEvent.NavigateToResults -> onNavigateToResults(event.isVictory)
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave Game?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to leave? Your current puzzle progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GameRedTimer)
                ) {
                    Text("Leave", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }

    if (showExtraHintsDialog) {
        AlertDialog(
            onDismissRequest = { showExtraHintsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = GameOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Need More Hints?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("You've used all 3 hints for this puzzle! Choose an option below to get more:")
                    Spacer(modifier = Modifier.height(14.dp))
                    val isOnline = NetworkMonitor.isOnline(context)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isOnline) {
                                showExtraHintsDialog = false
                                context.findActivity()?.let { act ->
                                    viewModel.watchAdForHints(act)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOnline) Color(0xFFFFF7ED) else Color(0xFFF1F5F9)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isOnline) GameOrange else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Watch Ad", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GameDarkText)
                                Text(
                                    text = if (isOnline) "Free • Earn +2 Hints" else "No connection • Connect to watch",
                                    fontSize = 12.sp,
                                    color = if (isOnline) GameSecondaryText else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (userCoins >= 30) {
                                    showExtraHintsDialog = false
                                    viewModel.buyHintWithCoins()
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (userCoins >= 30) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GameBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Buy with Coins", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GameDarkText)
                                Text(if (userCoins >= 30) "30 Coins • +1 Hint" else "Need 30 Coins (You have $userCoins)", fontSize = 12.sp, color = GameSecondaryText)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExtraHintsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFreeCoinsDialog) {
        FreeCoinsDialog(
            onDismiss = { showFreeCoinsDialog = false },
            onWatchAd = {
                showFreeCoinsDialog = false
                context.findActivity()?.let { act ->
                    viewModel.watchAdForCoins(act)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            SoundManager.play(SoundManager.SoundType.TAP)
                            showExitDialog = true
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Game",
                            tint = GameDarkText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${uiState.category?.emoji ?: "🧩"} ${uiState.category?.name ?: ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = GameDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sound toggle
                    IconButton(
                        onClick = {
                            SoundManager.isMuted = !SoundManager.isMuted
                            muted = SoundManager.isMuted
                            if (!muted) SoundManager.play(SoundManager.SoundType.TAP)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = if (muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Sound",
                            tint = GameDarkText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Coins badge with clickable + button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .clickable {
                                SoundManager.play(SoundManager.SoundType.TAP)
                                showFreeCoinsDialog = true
                            }
                            .padding(start = 8.dp, end = 5.dp, top = 3.dp, bottom = 3.dp)
                            .testTag("game_coins_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GameOrange,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userCoins",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GameDarkText,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(GameOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Get Free Coins",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Game Stats Bar: Timer, Score, Hint Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("game_stats_bar"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer pill
                    val isLowTime = uiState.mode == GameMode.TIME && uiState.timeRemainingSeconds < 30
                    val timerText = if (uiState.mode == GameMode.CLASSIC) {
                        val mins = uiState.elapsedTimeSeconds / 60
                        val secs = uiState.elapsedTimeSeconds % 60
                        String.format("%02d:%02d", mins, secs)
                    } else {
                        val mins = uiState.timeRemainingSeconds / 60
                        val secs = uiState.timeRemainingSeconds % 60
                        String.format("%02d:%02d", mins, secs)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLowTime) GameRedTimerBg else Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("timer_pill")
                    ) {
                        Text(
                            text = timerText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isLowTime) GameRedTimer else GameDarkText
                        )
                    }

                    // Score pill
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameSecondaryText
                        )
                        Text(
                            text = "${uiState.score}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = GameBlue,
                            modifier = Modifier.testTag("score_counter")
                        )
                    }

                    // Words remaining progress
                    val foundCount = uiState.foundWords.size
                    val totalWords = uiState.grid?.words?.size ?: 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "WORDS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameSecondaryText
                        )
                        Text(
                            text = "$foundCount/$totalWords",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GameDarkText
                        )
                    }

                    // Hint button with red badge
                    Box {
                        IconButton(
                            onClick = {
                                if (uiState.hintsRemaining > 0) {
                                    viewModel.useHint()
                                } else {
                                    showExtraHintsDialog = true
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (uiState.hintsRemaining > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9))
                                .testTag("hint_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = if (uiState.hintsRemaining > 0) GameOrange else GameGrayedFoundWord,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Red Badge
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (uiState.hintsRemaining > 0) Color(0xFFEF4444) else Color(0xFF94A3B8))
                                .align(Alignment.TopEnd)
                                .testTag("hint_badge"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.hintsRemaining}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Letter Grid View
            val grid = uiState.grid
            if (grid != null) {
                LetterGridView(
                    grid = grid,
                    foundWords = uiState.foundWords,
                    selectedPath = uiState.selectedPath,
                    hintFlashCells = uiState.hintFlashCells,
                    hintFlashActive = uiState.hintFlashActive,
                    onDragStart = { start ->
                        viewModel.onSelectionDrag(start, start)
                    },
                    onDrag = { start, current ->
                        viewModel.onSelectionDrag(start, current)
                    },
                    onDragEnd = {
                        viewModel.onSelectionEnd()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Word List Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("word_list_panel"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Find Words (${uiState.foundWords.size}/${uiState.grid?.words?.size ?: 0})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GameDarkText
                        )

                        if (uiState.bonusWordsFound.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = GamePink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.bonusWordsFound.size} Bonus",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GamePink
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val foundWordsSet = remember(uiState.foundWords) {
                        uiState.foundWords.map { it.word.uppercase() }.toSet()
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.grid?.words?.forEach { placedWord ->
                            val isFound = foundWordsSet.contains(placedWord.word.uppercase())
                            val highlightColor = if (isFound) {
                                WordHighlightColors.getOrElse(placedWord.colorIndex % WordHighlightColors.size) {
                                    GameBlue
                                }
                            } else null

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isFound)
                                            highlightColor?.copy(alpha = 0.18f) ?: Color(0xFFF1F5F9)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("word_chip_${placedWord.word}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isFound) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = highlightColor ?: GameDarkText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = placedWord.word,
                                        fontWeight = if (isFound) FontWeight.Normal else FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isFound) GameGrayedFoundWord else GameDarkText,
                                        textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bonus Word Floating Toast
        AnimatedVisibility(
            visible = uiState.latestBonusWord != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(GamePink)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("bonus_word_toast")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bonus Word: ${uiState.latestBonusWord} (+5 pts!)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
