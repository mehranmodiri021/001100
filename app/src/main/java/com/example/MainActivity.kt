package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ArenaViewModel
import com.example.ui.viewmodel.ArenaViewModelFactory

class MainActivity : ComponentActivity() {

    private val app by lazy { application as ArenaClashApplication }

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
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArenaApp(
                        viewModel = viewModel,
                        purchaseLauncher = purchaseLauncher
                    )
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

@Composable
fun ArenaApp(
    viewModel: ArenaViewModel,
    purchaseLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
) {
    var isSplashFinished by remember { mutableStateOf(false) }

    Crossfade(
        targetState = isSplashFinished,
        label = "SplashToMainCrossfade"
    ) { finished ->
        if (!finished) {
            SplashScreen(
                onSplashFinished = { isSplashFinished = true }
            )
        } else {
            MainScreen(
                viewModel = viewModel,
                purchaseLauncher = purchaseLauncher
            )
        }
    }
}
