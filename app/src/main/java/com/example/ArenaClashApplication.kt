package com.arenaclash.game

import android.app.Application
import com.arenaclash.game.ads.TapsellManager
import com.arenaclash.game.billing.BazaarBillingManager
import com.arenaclash.game.data.local.AppDatabase
import com.arenaclash.game.data.repository.GameRepository
import com.arenaclash.game.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ArenaClashApplication : Application() {

    // Scope سراسری برای عملیات async (Room, Coroutines)
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // دیتابیس Room — فقط یک بار ساخته می‌شود (lazy)
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    // Repository کاربر (پروفایل، VIP و...)
    val userRepository by lazy {
        UserRepository(
            userDao = database.userDao(),
            vipDao = database.vipDao()
        )
    }

    // Repository بازی (چالش‌ها، جوایز، جدول امتیازات و...)
    val gameRepository by lazy {
        GameRepository(
            challengeDao = database.challengeDao(),
            rewardDao = database.rewardDao(),
            leaderboardDao = database.leaderboardDao(),
            matchHistoryDao = database.matchHistoryDao(),
            settingsDao = database.settingsDao()
        )
    }

    // مدیریت پرداخت درون‌برنامه‌ای کافه‌بازار (Poolakey)
    val billingManager by lazy { BazaarBillingManager(this) }

    override fun onCreate() {
        super.onCreate()

        // ─── مقداردهی اولیه تپسل پلاس ───
        // TapsellManager یک object است، پس نیازی به getInstance() نیست
        TapsellManager.initialize(this)
    }
}
