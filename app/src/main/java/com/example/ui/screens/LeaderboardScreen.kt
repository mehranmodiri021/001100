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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ArenaViewModel

@Composable
fun LeaderboardScreen(
    viewModel: ArenaViewModel
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val vipState by viewModel.vipState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
            .testTag("leaderboard_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Leaderboard Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFB45309), Color(0xFF1E1B4B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "جدول رده‌بندی قهرمانان",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "برترین بازیکنان این فصل آرنا بر اساس کاپ‌های کسب شده",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            color = Color(0xFFF59E0B).copy(alpha = 0.25f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "رتبه‌بندی کل فصل",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(leaderboard) { entry ->
            val isCurrentUser = entry.isCurrentUser
            val isUserVip = if (isCurrentUser) (vipState?.isActive == true) else entry.isVip

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_row_${entry.rank}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentUser) Color(0xFF1E3A8A) else Color(0xFF1E293B)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 6.dp else 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Badge
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = when (entry.rank) {
                                1 -> Color(0xFFEAB308) // Gold
                                2 -> Color(0xFF94A3B8) // Silver
                                3 -> Color(0xFFCD7F32) // Bronze
                                else -> Color(0xFF334155)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${entry.rank}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (entry.rank in 1..3) Color.Black else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.username,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isCurrentUser) Color(0xFF93C5FD) else Color.White
                                )
                                if (isUserVip) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFF59E0B),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "VIP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            if (isCurrentUser) {
                                Text(
                                    text = "جایگاه فعلی شما",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF60A5FA)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${entry.trophies}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
