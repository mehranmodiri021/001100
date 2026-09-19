package com.example

import android.app.Application
import android.util.Log
import com.example.ads.TapsellManager
import com.example.billing.BazaarBillingManager
import com.example.data.local.AppDatabase
import com.example.data.repository.GameRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ArenaClashApplication : Application() {

    companion object {
        private const val TAG = "ArenaClashApplication"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val userRepository by lazy { UserRepository(database.userDao(), database.vipDao()) }
    val gameRepository by lazy {
        GameRepository(
            database.challengeDao(),
            database.rewardDao(),
            database.leaderboardDao(),
            database.matchHistoryDao(),
            database.settingsDao()
        )
    }
    val billingManager by lazy { BazaarBillingManager(this) }
    val tapsellManager by lazy { TapsellManager.getInstance() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ArenaClashApplication onCreate: Initializing services...")
        // Initialize Tapsell Plus early in the Application lifecycle
        tapsellManager.initialize(this) { success ->
            Log.d(TAG, "Tapsell initialization completed with status: $success")
        }
    }
}
