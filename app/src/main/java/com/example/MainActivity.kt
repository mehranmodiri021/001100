package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.ArenaViewModel
import com.example.ui.viewmodel.ArenaViewModelFactory

class MainActivity : ComponentActivity() {

    private val app by lazy { application as ArenaClashApplication }

    // Register real Bazaar buy flow launcher
    private val purchaseLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        app.billingManager.handleActivityResult(result)
    }

    private val viewModel: ArenaViewModel by viewModels {
        ArenaViewModelFactory(
            userRepository = app.userRepository,
            gameRepository = app.gameRepository,
            billingManager = app.billingManager,
            tapsellManager = app.tapsellManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val customColorScheme = darkColorScheme(
                primary = Color(0xFFF59E0B),
                onPrimary = Color.Black,
                secondary = Color(0xFF38BDF8),
                onSecondary = Color.Black,
                background = Color(0xFF0B0F19),
                onBackground = Color.White,
                surface = Color(0xFF1E293B),
                onSurface = Color.White
            )

            MaterialTheme(colorScheme = customColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isSplashFinished by remember { mutableStateOf(false) }

                    if (!isSplashFinished) {
                        SplashScreen(
                            onSplashFinished = {
                                isSplashFinished = true
                            }
                        )
                    } else {
                        MainScreen(
                            viewModel = viewModel,
                            purchaseLauncher = purchaseLauncher
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        app.billingManager.connectToBazaarService()
    }

    override fun onDestroy() {
        super.onDestroy()
        app.billingManager.disconnect()
    }
}
