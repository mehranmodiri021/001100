package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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

data class ArenaStage(
    val name: String,
    val description: String,
    val minTrophies: Int,
    val ticketCost: Int,
    val rewardMultiplier: String,
    val accentColor: Color
)

val ARENA_STAGES = listOf(
    ArenaStage("میدان تمرینی نبرد", "مناسب برای مبارزان تازه‌کار و ارتقای مهارت", 0, 1, "1x", Color(0xFF38BDF8)),
    ArenaStage("صحرای آتشین", "نبرد تند و پرحرارت با مبارزان سطح متوسط", 1000, 2, "2x", Color(0xFFF97316)),
    ArenaStage("قلعه قهرمانان", "میدان بزرگان آرنا با پاداش‌های شگفت‌انگیز", 2000, 3, "3.5x", Color(0xFFF59E0B)),
    ArenaStage("آرنای افسانه‌ای", "ویژه گلادیاتورهای برتر فصل و اعضای VIP", 3000, 5, "5x", Color(0xFFA855F7))
)

@Composable
fun ArenaScreen(
    viewModel: ArenaViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()
    val vipState by viewModel.vipState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("arena_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Status Card
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
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape),
                                color = if (vipState?.isActive == true) Color(0xFFF59E0B) else Color(0xFF38BDF8)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (vipState?.isActive == true) Icons.Default.Stars else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userProfile?.username ?: "جنگجوی آرنا",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    if (vipState?.isActive == true) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFF59E0B),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "VIP",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "سطح ${userProfile?.level ?: 1} • کاپ: ${userProfile?.trophies ?: 0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // XP Bar
                        val currentXp = userProfile?.xp ?: 0
                        val targetXp = userProfile?.xpToNextLevel ?: 2000
                        val progressRatio = (currentXp.toFloat() / targetXp.toFloat()).coerceIn(0f, 1f)

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "پیشرفت به سطح بعدی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "$currentXp / $targetXp XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Currencies Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CurrencyBadge(
                                icon = Icons.Default.MonetizationOn,
                                count = "${userProfile?.coins ?: 0}",
                                label = "سکه طلا",
                                tint = Color(0xFFF59E0B)
                            )
                            CurrencyBadge(
                                icon = Icons.Default.LocalActivity,
                                count = "${userProfile?.tickets ?: 0}",
                                label = "بلیط نبرد",
                                tint = Color(0xFF38BDF8)
                            )
                            CurrencyBadge(
                                icon = Icons.Default.EmojiEvents,
                                count = "${userProfile?.victories ?: 0}",
                                label = "پیروزی‌ها",
                                tint = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "انتخاب میدان مبارزه",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(ARENA_STAGES) { stage ->
            val userTrophies = userProfile?.trophies ?: 0
            val isUnlocked = userTrophies >= stage.minTrophies
            val userTickets = userProfile?.tickets ?: 0
            val canAfford = userTickets >= stage.ticketCost

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stage_card_${stage.name}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF141A29)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = stage.accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = stage.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stage.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isUnlocked) Color.White else Color(0xFF64748B)
                            )
                            Text(
                                text = stage.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = "ضریب ${stage.rewardMultiplier}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = stage.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUnlocked) "هزینه ورود: ${stage.ticketCost} بلیط" else "نیاز به ${stage.minTrophies} کاپ",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUnlocked) Color(0xFF7DD3FC) else Color(0xFFEF4444)
                        )

                        Button(
                            onClick = {
                                viewModel.startBattle(stage.name, stage.ticketCost)
                            },
                            enabled = isUnlocked && canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = stage.accentColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_enter_${stage.name}")
                        ) {
                            Text(
                                text = if (!isUnlocked) "قفل" else if (!canAfford) "بلیط کم است" else "ورود به نبرد",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "آخرین مبارزات شما",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (matchHistory.isEmpty()) {
            item {
                Text(
                    text = "هنوز نبردی ثبت نشده است. اولین مبارزه خود را آغاز کنید!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            items(matchHistory.take(4)) { match ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (match.isVictory) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (match.isVictory) "برد" else "باخت",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (match.isVictory) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حریف: ${match.opponentName}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = match.arenaName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${match.coinsDelta} سکه",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = if (match.trophiesDelta >= 0) "+${match.trophiesDelta} کاپ" else "${match.trophiesDelta} کاپ",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (match.trophiesDelta >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
        )
    }
}
