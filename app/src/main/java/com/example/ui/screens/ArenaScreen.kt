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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

data class ArenaTier(
    val id: Int,
    val title: String,
    val subtitle: String,
    val minTrophies: Int,
    val entryTicket: Int,
    val coinBet: Int,
    val accentColor: Color
)

@Composable
fun ArenaScreen(
    viewModel: ArenaViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val vipState by viewModel.vipState.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val tiers = listOf(
        ArenaTier(1, "آرنای نوآموزان (برنزی)", "میدان آموزش و تمرین مبارزه", 0, 1, 50, Color(0xFFCD7F32)),
        ArenaTier(2, "آرنای شن‌های روان (نقره‌ای)", "نبرد تاکتیکی و سریع", 400, 1, 150, Color(0xFF94A3B8)),
        ArenaTier(3, "آرنای آتشین (طلایی)", "ویژه جنگجویان با تجربه و رقابتی", 800, 1, 400, Color(0xFFF59E0B)),
        ArenaTier(4, "آرنای اسطوره‌ای (الماس)", "بالاترین سطح نبرد با جایزه ۲ برابری", 1500, 1, 1000, Color(0xFF38BDF8))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
            .testTag("arena_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Arena Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("arena_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF831843), Color(0xFF1E1B4B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "میدان نبرد قهرمانان",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            if (vipState?.isActive == true) {
                                Surface(
                                    color = Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stars,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "VIP فعال",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "با انتخاب هر آرنا وارد مبارزه آنلاین شوید. پیروزی در هر میدان، سکه و کاپ قهرمانی برای شما به ارمغان می‌آورد!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatPill(
                                icon = Icons.Default.EmojiEvents,
                                label = "کاپ‌های شما",
                                value = "${userProfile?.trophies ?: 0}",
                                tint = Color(0xFFFBBF24)
                            )
                            StatPill(
                                icon = Icons.Default.LocalActivity,
                                label = "بلیط نبرد",
                                value = "${userProfile?.tickets ?: 0}",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "میدان‌های قابل انتخاب",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(tiers) { tier ->
            val userTrophies = userProfile?.trophies ?: 0
            val isUnlocked = userTrophies >= tier.minTrophies
            val userTickets = userProfile?.tickets ?: 0
            val canAfford = userTickets >= tier.entryTicket

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("arena_tier_${tier.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = tier.accentColor.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = tier.accentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = tier.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isUnlocked) Color.White else Color(0xFF64748B)
                            )
                            Text(
                                text = tier.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "جایزه: ${tier.coinBet * 2} سکه",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    if (isUnlocked) {
                        Button(
                            onClick = { viewModel.startBattle(tier.title, tier.coinBet) },
                            enabled = canAfford && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = tier.accentColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("battle_btn_${tier.id}")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalActivity,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "نبرد (۱)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "قفل (${tier.minTrophies} کاپ)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "تاریخچه نبردهای اخیر",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        if (matchHistory.isEmpty()) {
            item {
                Text(
                    text = "هنوز نبردی ثبت نشده است. اولین مبارزه خود را آغاز کنید!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(matchHistory) { match ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (match.isVictory) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (match.isVictory) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (match.isVictory) "پیروزی برابر ${match.opponentName}" else "شکست برابر ${match.opponentName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "نتیجه: ${match.playerScore} - ${match.opponentScore}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (match.trophiesDelta >= 0) "+${match.trophiesDelta} کاپ" else "${match.trophiesDelta} کاپ",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (match.trophiesDelta >= 0) Color(0xFFFBBF24) else Color(0xFFEF4444)
                            )
                            if (match.coinsEarned > 0) {
                                Text(
                                    text = "+${match.coinsEarned} سکه",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        color = Color.Black.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        }
    }
}
