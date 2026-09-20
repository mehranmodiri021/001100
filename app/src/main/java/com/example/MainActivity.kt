package com.arenaclash.game

import android.os.Bundle
import android.util.Log
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
import com.arenaclash.game.ui.screens.MainScreen
import com.arenaclash.game.ui.screens.SplashScreen
import com.arenaclash.game.ui.theme.MyApplicationTheme
import com.arenaclash.game.ui.viewmodel.ArenaViewModel
import com.arenaclash.game.ui.viewmodel.ArenaViewModelFactory

class MainActivity : ComponentActivity() {

    private val app by lazy { application as ArenaClashApplication }

    // لانچر برای دریافت نتیجه خرید از کافه‌بازار (AIDL-based)
    private val purchaseLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        app.billingManager.handleActivityResult(result)
    }

    private val viewModel: ArenaViewModel by viewModels {
        ArenaViewModelFactory(
            userRepository = app.userRepository,
            gameRepository = app.gameRepository,
            billingManager = app.billingManager
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
        // اتصال به سرویس پرداخت کافه‌بازار
        app.billingManager.connectToBazaarService()
        Log.d(TAG, "Attempting to connect to Bazaar billing service")
    }

    override fun onDestroy() {
        super.onDestroy()
        // قطع ارتباط با سرویس پرداخت
        app.billingManager.disconnect()
    }

    companion object {
        private const val TAG = "MainActivity"
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
