package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        delay(100)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF020617)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = Color(0xFFF59E0B),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "لوگوی بازی آرنا کلش",
                        tint = Color.Black,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Arena Clash",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = Color(0xFFF59E0B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "آرنا کلش",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "نبردهای حماسی آنلاین و جوایز کافه‌بازار",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .width(220.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFF59E0B),
                trackColor = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "در حال بارگذاری میدان‌های نبرد...",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "سازنده:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "سیدحمید موسوی زاده",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFE2E8F0)
                )
            }
        }
    }
}
