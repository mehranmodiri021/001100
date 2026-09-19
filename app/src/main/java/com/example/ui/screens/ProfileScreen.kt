package com.example.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.BazaarConfig
import com.example.ui.viewmodel.ArenaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ArenaViewModel,
    purchaseLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val vipState by viewModel.vipState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("profile_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // User ID Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape),
                        color = if (vipState?.isActive == true) Color(0xFFF59E0B) else Color(0xFF38BDF8)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (vipState?.isActive == true) Icons.Default.Stars else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile?.username ?: "جنگجوی آرنا",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "سطح ${userProfile?.level ?: 1} • مجموع مبارزات: ${userProfile?.totalMatches ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // VIP Status Indicator
                    if (vipState?.isActive == true) {
                        val expiryDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                            .format(Date(vipState?.expiresAt ?: 0L))
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "اشتراک VIP طلایی فعال است (انقضا: $expiryDate)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "حساب کاربری عادی (بدون اشتراک VIP)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Restore Purchases Button
            OutlinedButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_restore_purchases"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بازیابی خریدهای قبلی از کافه بازار",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Text(
                text = "فروشگاه درون‌برنامه‌ای کافه‌بازار",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // VIP Monthly Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_vip_monthly"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اشتراک VIP یک ماهه",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "نشان طلایی + ۲ برابر پاداش نبردها",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.buyProduct(purchaseLauncher, BazaarConfig.PRODUCT_VIP_MONTHLY)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_buy_vip_monthly")
                    ) {
                        Text(text = "خرید", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // VIP Yearly Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_vip_yearly"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFA855F7).copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اشتراک VIP یک ساله (ویژه)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "۳۶۵ روز دسترسی طلایی با تخفیف ویژه",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.buyProduct(purchaseLauncher, BazaarConfig.PRODUCT_VIP_YEARLY)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA855F7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_buy_vip_yearly")
                    ) {
                        Text(text = "خرید", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Coins 1000 Pack
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_coins_1000"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "بسته ۱۰۰۰ سکه طلا",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "مناسب برای ارتقای سریع قهرمانان",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.buyProduct(purchaseLauncher, BazaarConfig.PRODUCT_COINS_1000)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_buy_coins_1000")
                    ) {
                        Text(text = "خرید", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Coins 5000 Pack
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_coins_5000"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "کیسه ۵۰۰۰ سکه طلا",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "بسته اقتصادی ویژه با پاداش بیشتر",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.buyProduct(purchaseLauncher, BazaarConfig.PRODUCT_COINS_5000)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_buy_coins_5000")
                    ) {
                        Text(text = "خرید", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tickets 10 Pack
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_tickets_10"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalActivity,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "بسته ۱۰ بلیط نبرد",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "ورود بدون محدودیت به میدان‌های آرنا",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.buyProduct(purchaseLauncher, BazaarConfig.PRODUCT_TICKETS_10)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF38BDF8),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_buy_tickets_10")
                    ) {
                        Text(text = "خرید", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
