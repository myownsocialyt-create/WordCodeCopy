package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.findActivity
import com.example.data.Category
import com.example.data.CategoryRepository
import com.example.engine.SoundManager
import com.example.model.GameMode
import com.example.ui.theme.GameBackground
import com.example.ui.theme.GameBlue
import com.example.ui.theme.GameDarkText
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GamePink
import com.example.ui.theme.GameSecondaryText
import com.example.ui.theme.GameSuccessGreen
import com.example.ui.viewmodel.GameUiState

@Composable
fun ResultsScreen(
    uiState: GameUiState,
    onPlayAgain: () -> Unit,
    onNextLevel: (Category) -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = uiState.category
    val nextCategory = category?.let { CategoryRepository.getNextCategory(it.id) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Trigger Interstitial Ad check (every 2nd eligible trigger, 60s cap)
        context.findActivity()?.let { activity ->
            AdManager.showInterstitialIfEligible(activity) {
                // Callback when ad completes or skipped
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Trophy / Result Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.isVictory)
                            Brush.radialGradient(listOf(Color(0xFFFEF08A), Color(0xFFFBBF24)))
                        else
                            Brush.radialGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isVictory) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                    contentDescription = null,
                    tint = if (uiState.isVictory) Color(0xFFB45309) else Color(0xFF475569),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isVictory) "LEVEL COMPLETED!" else "TIME'S UP!",
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                letterSpacing = 1.sp,
                color = if (uiState.isVictory) GameDarkText else Color(0xFFDC2626),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${category?.emoji ?: "🧩"} ${category?.name ?: "Puzzle"} • ${uiState.mode.displayName}",
                fontSize = 14.sp,
                color = GameSecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Score & Coins Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("results_summary_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameSecondaryText
                    )
                    Text(
                        text = "${uiState.score}",
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp,
                        color = GameBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Coins Earned
                        StatItem(
                            icon = Icons.Default.MonetizationOn,
                            iconTint = GameOrange,
                            label = "Coins Earned",
                            value = "+${uiState.coinsEarned}"
                        )

                        // Time Taken / Left
                        val formattedTime = if (uiState.mode == GameMode.CLASSIC) {
                            val mins = uiState.elapsedTimeSeconds / 60
                            val secs = uiState.elapsedTimeSeconds % 60
                            String.format("%02d:%02d", mins, secs)
                        } else {
                            val mins = uiState.timeRemainingSeconds / 60
                            val secs = uiState.timeRemainingSeconds % 60
                            String.format("%02d:%02d left", mins, secs)
                        }

                        StatItem(
                            icon = Icons.Default.Timer,
                            iconTint = GameBlue,
                            label = "Time",
                            value = formattedTime
                        )

                        // Bonus Words
                        StatItem(
                            icon = Icons.Default.Stars,
                            iconTint = GamePink,
                            label = "Bonus Words",
                            value = "${uiState.bonusWordsFound.size}"
                        )
                    }

                    if (uiState.bonusTimeScore > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⚡ Speed Bonus: +${uiState.bonusTimeScore} pts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons: Play Again / Next Level / Home
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.isVictory && nextCategory != null) {
                Button(
                    onClick = {
                        SoundManager.play(SoundManager.SoundType.TAP)
                        onNextLevel(nextCategory)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("next_level_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GameSuccessGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NEXT LEVEL: ${nextCategory.name} ${nextCategory.emoji}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Button(
                onClick = {
                    SoundManager.play(SoundManager.SoundType.TAP)
                    onPlayAgain()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("play_again_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GameBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY AGAIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    SoundManager.play(SoundManager.SoundType.TAP)
                    onHome()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("home_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = GameDarkText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BACK TO HOME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkText
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = GameDarkText
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = GameSecondaryText
        )
    }
}
