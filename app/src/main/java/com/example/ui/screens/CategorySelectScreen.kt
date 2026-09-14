package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.CategoryRepository
import com.example.engine.SoundManager
import com.example.ui.components.TopCoinBar
import com.example.ui.components.UnlockLevelDialog
import com.example.ui.theme.GameBackground
import com.example.ui.theme.GameBlue
import com.example.ui.theme.GameDarkText
import com.example.ui.theme.GameGrayedFoundWord
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GameSecondaryText
import com.example.ui.theme.GameSuccessGreen

@Composable
fun CategorySelectScreen(
    coins: Int,
    completedLevels: Set<String>,
    unlockedLevels: Set<String>,
    onCategorySelected: (Category) -> Unit,
    onUnlockWithCoins: (Category) -> Unit,
    onWatchAdToEarnCoins: () -> Unit,
    onBack: () -> Unit,
    onAddCoinsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var categoryToUnlock by remember { mutableStateOf<Pair<Category, Int>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        TopCoinBar(
            coins = coins,
            onBack = onBack,
            title = "Select Category",
            onAddCoinsClick = onAddCoinsClick
        )

        Text(
            text = "Choose a theme to play (${CategoryRepository.CATEGORIES.size} total)",
            fontSize = 13.sp,
            color = GameSecondaryText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("category_grid")
        ) {
            itemsIndexed(CategoryRepository.CATEGORIES) { index, category ->
                // Unlocked if: 1st category, or unlocked via coins, or preceding category completed
                val isUnlocked = (index == 0) ||
                        unlockedLevels.contains(category.id) ||
                        completedLevels.contains(CategoryRepository.CATEGORIES[index - 1].id)
                val isCompleted = completedLevels.contains(category.id)

                CategoryCard(
                    category = category,
                    index = index,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    onClick = {
                        if (isUnlocked) {
                            SoundManager.play(SoundManager.SoundType.TAP)
                            onCategorySelected(category)
                        } else {
                            SoundManager.play(SoundManager.SoundType.TAP)
                            categoryToUnlock = Pair(category, index)
                        }
                    }
                )
            }
        }
    }

    categoryToUnlock?.let { (cat, index) ->
        UnlockLevelDialog(
            category = cat,
            levelIndex = index,
            coins = coins,
            onDismiss = { categoryToUnlock = null },
            onUnlockWithCoins = {
                categoryToUnlock = null
                onUnlockWithCoins(cat)
            },
            onWatchAdToEarnCoins = {
                categoryToUnlock = null
                onWatchAdToEarnCoins()
            }
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    index: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isUnlocked) Color.White else Color(0xFFF1F2F6)
    val textColor = if (isUnlocked) GameDarkText else GameGrayedFoundWord

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("category_card_${category.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 1.dp),
        border = if (isUnlocked && isCompleted) {
            androidx.compose.foundation.BorderStroke(1.5.dp, GameSuccessGreen.copy(alpha = 0.5f))
        } else if (!isUnlocked) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E4EB))
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Level index badge in top corner
            Text(
                text = "#${index + 1}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) GameSecondaryText.copy(alpha = 0.6f) else GameGrayedFoundWord,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Status icon (Lock or Completed checkmark)
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked - Tap to unlock",
                        tint = GameOrange,
                        modifier = Modifier.size(15.dp)
                    )
                }
            } else if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = GameSuccessGreen,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category Emoji
                Text(
                    text = category.emoji,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isUnlocked) {
                    Text(
                        text = "${category.words.size} Words",
                        fontSize = 12.sp,
                        color = GameSecondaryText,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = GameOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Unlock (300)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameOrange,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
