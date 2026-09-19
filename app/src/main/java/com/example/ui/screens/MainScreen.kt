package com.example.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.ArenaViewModel
import com.example.ui.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ArenaViewModel,
    purchaseLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Arena.route

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.BattleFinished -> {
                    val msg = if (event.isVictory) {
                        "پیروزی درخشان! +${event.coinsEarned} سکه، +${event.trophiesDelta} کاپ به دست آوردید."
                    } else {
                        "شکست در نبرد. +${event.coinsEarned} سکه تسلی‌بخش، ${event.trophiesDelta} کاپ کسر شد."
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            Screen.Arena.route -> "آرنا کلش • میدان نبرد"
                            Screen.Challenges.route -> "چالش‌ها و ماموریت‌ها"
                            Screen.Rewards.route -> "صندوق‌ها و جوایز رایگان"
                            Screen.Leaderboard.route -> "جدول برترین گلادیاتورها"
                            Screen.Profile.route -> "پروفایل و فروشگاه VIP"
                            Screen.Settings.route -> "تنظیمات بازی"
                            else -> "آرنا کلش"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    if (screen != null) {
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.titleFa,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.titleFa,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color(0xFFF59E0B),
                                indicatorColor = Color(0xFFF59E0B),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B0F19))
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Arena.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Arena.route) {
                    ArenaScreen(viewModel = viewModel)
                }
                composable(Screen.Challenges.route) {
                    ChallengesScreen(viewModel = viewModel)
                }
                composable(Screen.Rewards.route) {
                    RewardsScreen(viewModel = viewModel)
                }
                composable(Screen.Leaderboard.route) {
                    LeaderboardScreen(viewModel = viewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        purchaseLauncher = purchaseLauncher
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
