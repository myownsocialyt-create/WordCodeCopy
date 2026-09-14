package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.engine.SoundManager
import com.example.engine.WordSearchGenerator
import com.example.model.GameMode
import com.example.ui.components.TopCoinBar
import com.example.ui.theme.GameBackground
import com.example.ui.theme.GameBlue
import com.example.ui.theme.GameDarkText
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GamePink
import com.example.ui.theme.GameSecondaryText

@Composable
fun ModeSelectScreen(
    category: Category,
    coins: Int,
    onStartGame: (Category, GameMode) -> Unit,
    onBack: () -> Unit,
    onAddCoinsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }
    val gridSize = remember(category) { WordSearchGenerator.calculateGridSize(category.words) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        TopCoinBar(
            coins = coins,
            onBack = onBack,
            title = "Game Mode",
            onAddCoinsClick = onAddCoinsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Category Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("mode_category_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.emoji,
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = category.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = GameDarkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${category.words.size} Hidden Words • ${gridSize}x${gridSize} Grid",
                                fontSize = 13.sp,
                                color = GameSecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Mode",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GameDarkText
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Classic Mode Card
                ModeOptionCard(
                    mode = GameMode.CLASSIC,
                    isSelected = selectedMode == GameMode.CLASSIC,
                    title = "Classic Mode",
                    badge = "Relaxed",
                    description = "No time limit. Timer counts UP from 0:00. Take your time to find all words.",
                    icon = Icons.Default.HourglassBottom,
                    accentColor = GameBlue,
                    onSelect = {
                        SoundManager.play(SoundManager.SoundType.TAP)
                        selectedMode = GameMode.CLASSIC
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Time Mode Card
                ModeOptionCard(
                    mode = GameMode.TIME,
                    isSelected = selectedMode == GameMode.TIME,
                    title = "Time Mode",
                    badge = "2-Min Rush",
                    description = "2-minute countdown from 2:00. Time bonus points (+timeLeft × 2) awarded on completion!",
                    icon = Icons.Default.Schedule,
                    accentColor = GameOrange,
                    onSelect = {
                        SoundManager.play(SoundManager.SoundType.TAP)
                        selectedMode = GameMode.TIME
                    }
                )
            }

            // Start Button
            Button(
                onClick = {
                    SoundManager.play(SoundManager.SoundType.TAP)
                    onStartGame(category, selectedMode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("start_puzzle_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == GameMode.CLASSIC) GameBlue else GameOrange
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START PUZZLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    mode: GameMode,
    isSelected: Boolean,
    title: String,
    badge: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFFE2E4EB),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("mode_option_${mode.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.06f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GameDarkText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = GameSecondaryText
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor
                )
            )
        }
    }
}
