package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleFa: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Arena : Screen("arena", "آرنا", Icons.Filled.FlashOn, Icons.Outlined.FlashOn)
    object Challenges : Screen("challenges", "چالش‌ها", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Rewards : Screen("rewards", "جوایز", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard)
    object Leaderboard : Screen("leaderboard", "رده‌بندی", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard)
    object Profile : Screen("profile", "پروفایل", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
    object Settings : Screen("settings", "تنظیمات", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems: List<Screen> = listOf(
            Arena,
            Challenges,
            Rewards,
            Leaderboard,
            Profile,
            Settings
        )
    }
}
