package com.example

import android.app.Application
import com.example.ads.TapsellManager
import com.example.billing.BazaarBillingManager
import com.example.data.local.AppDatabase
import com.example.data.repository.GameRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ArenaClashApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val userRepository by lazy {
        UserRepository(
            userDao = database.userDao(),
            vipDao = database.vipDao()
        )
    }

    val gameRepository by lazy {
        GameRepository(
            challengeDao = database.challengeDao(),
            rewardDao = database.rewardDao(),
            leaderboardDao = database.leaderboardDao(),
            matchHistoryDao = database.matchHistoryDao(),
            settingsDao = database.settingsDao()
        )
    }

    val billingManager by lazy { BazaarBillingManager(this) }

    val tapsellManager by lazy { TapsellManager.getInstance() }

    override fun onCreate() {
        super.onCreate()
        tapsellManager.initialize(this)
    }
}
