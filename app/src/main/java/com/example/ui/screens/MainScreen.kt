package com.example.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.ArenaViewModel
import com.example.ui.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ArenaViewModel,
    purchaseLauncher: ActivityResultLauncher<IntentSenderRequest>?
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Arena) }
    val userProfile by viewModel.userProfile.collectAsState()
    val vipState by viewModel.vipState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.BattleFinished -> {
                    val msg = if (event.isVictory) {
                        "پیروزی در میدان نبرد! +${event.coinsEarned} سکه و +${event.trophiesDelta} کاپ به دست آوردید."
                    } else {
                        "شکست در میدان نبرد. ${event.trophiesDelta} کاپ کسر شد."
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            color = Color(0xFFF59E0B)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "آرنا کلش",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Coins badge
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userProfile?.coins ?: 0}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Tickets badge
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalActivity,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userProfile?.tickets ?: 0}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF7DD3FC)
                                )
                            }
                        }

                        if (vipState?.isActive == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFF59E0B),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "VIP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        ),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            // Exactly 6 bottom navigation items, all 6 visible and fully aligned
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = selectedScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreen = screen },
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
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFF59E0B),
                            selectedTextColor = Color(0xFFF59E0B),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedScreen, label = "screen_transition") { screen ->
                when (screen) {
                    Screen.Arena -> ArenaScreen(viewModel = viewModel)
                    Screen.Challenges -> ChallengesScreen(viewModel = viewModel)
                    Screen.Rewards -> RewardsScreen(viewModel = viewModel)
                    Screen.Leaderboard -> LeaderboardScreen(viewModel = viewModel)
                    Screen.Profile -> ProfileScreen(viewModel = viewModel, purchaseLauncher = purchaseLauncher)
                    Screen.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
