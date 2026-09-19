package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleFa: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Arena : Screen(
        route = "arena",
        titleFa = "آرنا",
        selectedIcon = Icons.Filled.FlashOn,
        unselectedIcon = Icons.Outlined.FlashOn
    )

    data object Challenges : Screen(
        route = "challenges",
        titleFa = "چالش‌ها",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents
    )

    data object Rewards : Screen(
        route = "rewards",
        titleFa = "جوایز",
        selectedIcon = Icons.Filled.Redeem,
        unselectedIcon = Icons.Outlined.Redeem
    )

    data object Leaderboard : Screen(
        route = "leaderboard",
        titleFa = "رده‌بندی",
        selectedIcon = Icons.Filled.Leaderboard,
        unselectedIcon = Icons.Outlined.Leaderboard
    )

    data object Profile : Screen(
        route = "profile",
        titleFa = "پروفایل",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Settings : Screen(
        route = "settings",
        titleFa = "تنظیمات",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(
                Arena,
                Challenges,
                Rewards,
                Leaderboard,
                Profile,
                Settings
            )
    }
}
