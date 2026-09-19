package com.example.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
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
    purchaseLauncher: ActivityResultLauncher<IntentSenderRequest>?
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val vipState by viewModel.vipState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isVipActive = vipState?.isActive == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        // Avatar
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            color = if (isVipActive) Color(0xFFF59E0B) else Color(0xFF3B82F6)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile?.username ?: "قهرمان آرنا",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (isVipActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stars,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "VIP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "سطح ${userProfile?.level ?: 1} • جنگجوی میدان نبرد",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem(
                                icon = Icons.Default.MonetizationOn,
                                label = "سکه",
                                value = "${userProfile?.coins ?: 0}",
                                tint = Color(0xFFF59E0B)
                            )
                            ProfileStatItem(
                                icon = Icons.Default.LocalActivity,
                                label = "بلیط",
                                value = "${userProfile?.tickets ?: 0}",
                                tint = Color(0xFF38BDF8)
                            )
                            ProfileStatItem(
                                icon = Icons.Default.EmojiEvents,
                                label = "کاپ",
                                value = "${userProfile?.trophies ?: 0}",
                                tint = Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            }
        }

        // VIP Subscription Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vip_subscription_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVipActive) Color(0xFF292524) else Color(0xFF1E293B)
                ),
                border = if (isVipActive) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)) else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اشتراک طلایی VIP آرنا",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFBBF24)
                            )
                        }

                        Surface(
                            color = if (isVipActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isVipActive) "فعال" else "غیرفعال",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isVipActive) Color(0xFF34D399) else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isVipActive) {
                        val expiryDateStr = try {
                            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                            sdf.format(Date(vipState?.expiresAt ?: 0L))
                        } catch (_: Exception) {
                            "-"
                        }
                        Text(
                            text = "نوع اشتراک: ${if (vipState?.planType == "yearly") "یک ساله" else "۳۰ روزه"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "تاریخ انقضا: $expiryDateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "مزایای عضویت VIP: حذف کامل تبلیغات اجباری، دریافت ۲ برابری سکه‌ها در هر پیروزی، و نشان طلایی اختصاصی در جدول قهرمانان!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    purchaseLauncher?.let {
                                        viewModel.purchaseProduct(it, BazaarConfig.PRODUCT_VIP_MONTHLY)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("buy_vip_monthly_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "ماهانه (۳۰ روز)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black
                                )
                            }

                            Button(
                                onClick = {
                                    purchaseLauncher?.let {
                                        viewModel.purchaseProduct(it, BazaarConfig.PRODUCT_VIP_YEARLY)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("buy_vip_yearly_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "سالانه (۳۶۵ روز)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // In-App Purchases (Coins & Tickets)
        item {
            Text(
                text = "بسته‌های سکه و بلیط (پرداخت کافه بازار)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            ShopProductRow(
                title = "بسته ۱,۰۰۰ سکه نبرد",
                description = "افزایش فوری موجودی جهت ورود به آرناهای سطح بالا",
                productId = BazaarConfig.PRODUCT_COINS_1000,
                priceText = "خرید از بازار",
                icon = Icons.Default.MonetizationOn,
                accentColor = Color(0xFFF59E0B),
                onBuyClick = {
                    purchaseLauncher?.let {
                        viewModel.purchaseProduct(it, BazaarConfig.PRODUCT_COINS_1000)
                    }
                }
            )
        }

        item {
            ShopProductRow(
                title = "کیسه ۵,۰۰۰ سکه طلایی",
                description = "ارزش فوق‌العاده برای ارتقا و تجهیزات جنگجو",
                productId = BazaarConfig.PRODUCT_COINS_5000,
                priceText = "خرید از بازار",
                icon = Icons.Default.MonetizationOn,
                accentColor = Color(0xFFFBBF24),
                onBuyClick = {
                    purchaseLauncher?.let {
                        viewModel.purchaseProduct(it, BazaarConfig.PRODUCT_COINS_5000)
                    }
                }
            )
        }

        item {
            ShopProductRow(
                title = "بسته ۱۰ بلیط نبرد طلایی",
                description = "ورود بدون محدودیت به مسابقات و تورنمنت‌ها",
                productId = BazaarConfig.PRODUCT_TICKETS_10,
                priceText = "خرید از بازار",
                icon = Icons.Default.LocalActivity,
                accentColor = Color(0xFF38BDF8),
                onBuyClick = {
                    purchaseLauncher?.let {
                        viewModel.purchaseProduct(it, BazaarConfig.PRODUCT_TICKETS_10)
                    }
                }
            )
        }

        // Restore Purchases Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restore_purchases_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "بازیابی خریدهای قبلی",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "در صورت نصب مجدد برنامه، اشتراک VIP خریداری‌شده از کافه بازار بازیابی می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.restorePurchases() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("restore_purchases_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "بازیابی")
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
fun ProfileStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun ShopProductRow(
    title: String,
    description: String,
    productId: String,
    priceText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_row_$productId"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
        }
    }
}
