package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.engine.SoundManager
import com.example.ui.theme.GameBlue
import com.example.ui.theme.GameDarkText
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GameSecondaryText
import com.example.ui.theme.GameSuccessGreen
import com.example.util.NetworkMonitor

@Composable
fun UnlockLevelDialog(
    category: Category,
    levelIndex: Int,
    coins: Int,
    onDismiss: () -> Unit,
    onUnlockWithCoins: () -> Unit,
    onWatchAdToEarnCoins: () -> Unit
) {
    val context = LocalContext.current
    val isOnline = NetworkMonitor.isOnline(context)
    val hasEnoughCoins = coins >= 300

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (hasEnoughCoins) Color(0xFFE0F2FE) else Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasEnoughCoins) Icons.Default.LockOpen else Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (hasEnoughCoins) GameBlue else GameOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Unlock Level #${levelIndex + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = GameDarkText
                    )
                    Text(
                        text = "${category.name} ${category.emoji}",
                        fontSize = 13.sp,
                        color = GameSecondaryText
                    )
                }
            }
        },
        text = {
            Column {
                if (hasEnoughCoins) {
                    Text(
                        text = "Unlock this level now for 300 coins! You will be able to play it immediately and keep making progress.",
                        fontSize = 14.sp,
                        color = GameDarkText,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = GameSuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Cost: 300 Coins",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GameDarkText
                                )
                                Text(
                                    text = "Your balance: $coins coins (Remaining: ${coins - 300})",
                                    fontSize = 12.sp,
                                    color = GameSecondaryText
                                )
                            }
                        }
                    }
                } else {
                    // Not enough coins
                    Text(
                        text = "You need 300 coins to unlock this level. You currently have $coins coins.",
                        fontSize = 14.sp,
                        color = GameDarkText,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFDBA74)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OndemandVideo,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Watch Ad to Earn More Coins",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = GameDarkText
                                    )
                                    Text(
                                        text = "Earn +50 coins per short video!",
                                        fontSize = 12.sp,
                                        color = GameOrange,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (!isOnline) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "No internet connection to watch ad.",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (hasEnoughCoins) {
                Button(
                    onClick = {
                        SoundManager.play(SoundManager.SoundType.TAP)
                        onUnlockWithCoins()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GameSuccessGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_unlock_level_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unlock (300)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = {
                        SoundManager.play(SoundManager.SoundType.TAP)
                        onWatchAdToEarnCoins()
                    },
                    enabled = isOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameOrange,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("watch_ad_to_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OndemandVideo,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Ad (+50)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    SoundManager.play(SoundManager.SoundType.TAP)
                    onDismiss()
                }
            ) {
                Text(
                    text = "Cancel",
                    color = GameSecondaryText
                )
            }
        }
    )
}
