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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.entity.GameSettingsEntity
import com.example.ui.viewmodel.ArenaViewModel

@Composable
fun SettingsScreen(
    viewModel: ArenaViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val currentSettings = settings ?: GameSettingsEntity()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Settings Header
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
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
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
                                text = "تنظیمات بازی و حساب",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "شخصی‌سازی صداها، لرزش و اطلاعات نسخه برنامه",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            color = Color(0xFF64748B).copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "تنظیمات صوتی و بازخورد",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "جلوه‌های صوتی (Sound FX)",
                        subtitle = "صدای برخوردها و افکت‌های ضربات آرنا",
                        checked = currentSettings.soundEffects,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(currentSettings.copy(soundEffects = checked))
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        icon = Icons.Default.MusicNote,
                        title = "موسیقی پس‌زمینه (Music)",
                        subtitle = "موسیقی حماسی منوها و صحنه مبارزه",
                        checked = currentSettings.music,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(currentSettings.copy(music = checked))
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "لرزش (Haptic Feedback)",
                        subtitle = "ویبره گوشی هنگام اصابت ضربات مهلک",
                        checked = currentSettings.vibration,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(currentSettings.copy(vibration = checked))
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "اعلان‌ها (Notifications)",
                        subtitle = "یادآوری صندوقچه‌های رایگان و رویدادهای فصلی",
                        checked = currentSettings.notifications,
                        onCheckedChange = { checked ->
                            viewModel.updateSettings(currentSettings.copy(notifications = checked))
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "درباره برنامه و توسعه‌دهنده",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_app_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Arena Clash (آرنا کلش)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    InfoRow(label = "نسخه انتشار:", value = "1.0.0 (نسخه رسمی کافه‌بازار)")
                    InfoRow(label = "توسعه‌دهنده رسمی:", value = "سیدحمید موسوی زاده")
                    InfoRow(label = "موتور پرداخت درون‌برنامه‌ای:", value = "Cafe Bazaar In-App Billing (نسخه ۳)")
                    InfoRow(label = "پلتفرم تبلیغات ویدیویی:", value = "Tapsell Plus SDK 2.3.3")
                    InfoRow(label = "پایگاه داده محلی:", value = "Room Database (SQLite آفلاین)")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "تمامی حقوق مادی و معنوی این اثر متعلق به سیدحمید موسوی زاده می‌باشد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF10B981),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFFE2E8F0))
    }
}
