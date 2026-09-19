package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ChallengeDao
import com.example.data.local.dao.LeaderboardDao
import com.example.data.local.dao.MatchHistoryDao
import com.example.data.local.dao.RewardDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.VipDao
import com.example.data.local.entity.ChallengeItemEntity
import com.example.data.local.entity.GameSettingsEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.MatchHistoryEntity
import com.example.data.local.entity.RewardItemEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.VipStateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        VipStateEntity::class,
        RewardItemEntity::class,
        ChallengeItemEntity::class,
        LeaderboardEntryEntity::class,
        MatchHistoryEntity::class,
        GameSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun vipDao(): VipDao
    abstract fun rewardDao(): RewardDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arena_clash_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Check if tables are populated; if not, seed them
                        if (database.challengeDao().countChallenges() == 0) {
                            populateInitialData(database)
                        }
                        // Check for expired VIP
                        database.vipDao().checkAndExpireVip(System.currentTimeMillis())
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // Initial User Profile
                database.userDao().insertOrUpdateProfile(
                    UserProfileEntity(
                        id = 1,
                        username = "سیدحمید (شما)",
                        coins = 1200,
                        tickets = 6,
                        trophies = 750,
                        level = 4,
                        xp = 380,
                        avatarId = 1
                    )
                )

                // Initial VIP State (Non-VIP by default)
                database.vipDao().setVipState(
                    VipStateEntity(
                        id = 1,
                        isVip = false,
                        planType = "",
                        purchaseToken = "",
                        activatedAt = 0L,
                        expiresAt = 0L
                    )
                )

                // Initial Challenges
                val challenges = listOf(
                    ChallengeItemEntity(
                        id = 1,
                        title = "پیروزی در آرنای آتش",
                        description = "در ۳ مسابقه پیاپی پیروز شوید",
                        targetCount = 3,
                        currentCount = 1,
                        rewardCoins = 300,
                        rewardXp = 100
                    ),
                    ChallengeItemEntity(
                        id = 2,
                        title = "استاد ضربات مهلک",
                        description = "۵ ضربه بحرانی (Critical Hit) وارد کنید",
                        targetCount = 5,
                        currentCount = 3,
                        rewardCoins = 450,
                        rewardXp = 150
                    ),
                    ChallengeItemEntity(
                        id = 3,
                        title = "نبرد شبانه",
                        description = "در لیگ شبانه ۲ بار شرکت کنید",
                        targetCount = 2,
                        currentCount = 0,
                        rewardCoins = 600,
                        rewardXp = 200
                    ),
                    ChallengeItemEntity(
                        id = 4,
                        title = "قهرمان بیباک",
                        description = "با جان کمتر از ۲۰٪ پیروز میدان شوید",
                        targetCount = 1,
                        currentCount = 0,
                        rewardCoins = 800,
                        rewardXp = 300
                    )
                )
                database.challengeDao().insertChallenges(challenges)

                // Initial Rewards
                val rewards = listOf(
                    RewardItemEntity(id = 1, title = "پاداش ورود روزانه", rewardType = "coins", amount = 200, isClaimed = false),
                    RewardItemEntity(id = 2, title = "جعبه چوبی رایگان", rewardType = "chest", amount = 1, isClaimed = false),
                    RewardItemEntity(id = 3, title = "بلیط طلایی تورنمنت", rewardType = "tickets", amount = 2, isClaimed = false),
                    RewardItemEntity(id = 4, title = "کیسه سکه نبرد", rewardType = "coins", amount = 500, isClaimed = false),
                    RewardItemEntity(id = 5, title = "جعبه افسانهای آرنا", rewardType = "chest", amount = 1, isClaimed = false)
                )
                database.rewardDao().insertRewards(rewards)

                // Initial Leaderboard
                val leaderboard = listOf(
                    LeaderboardEntryEntity(id = 1, rank = 1, username = "سهراب_شکارچی", trophies = 2450, isVip = true),
                    LeaderboardEntryEntity(id = 2, rank = 2, username = "آرش_کمانگیر", trophies = 2310, isVip = true),
                    LeaderboardEntryEntity(id = 3, rank = 3, username = "طوفان_سرخ", trophies = 2190, isVip = false),
                    LeaderboardEntryEntity(id = 4, rank = 4, username = "سیدحمید (شما)", trophies = 750, isCurrentUser = true, isVip = false),
                    LeaderboardEntryEntity(id = 5, rank = 5, username = "شیر_بابل", trophies = 1980, isVip = true),
                    LeaderboardEntryEntity(id = 6, rank = 6, username = "عقاب_زاگرس", trophies = 1840, isVip = false),
                    LeaderboardEntryEntity(id = 7, rank = 7, username = "شوالیه_تاریک", trophies = 1720, isVip = false),
                    LeaderboardEntryEntity(id = 8, rank = 8, username = "مدافع_آرنا", trophies = 1610, isVip = false)
                )
                database.leaderboardDao().insertLeaderboard(leaderboard)

                // Initial Match History
                database.matchHistoryDao().insertMatch(
                    MatchHistoryEntity(
                        opponentName = "سهراب_شکارچی",
                        isVictory = true,
                        playerScore = 3,
                        opponentScore = 1,
                        coinsEarned = 150,
                        trophiesDelta = 25
                    )
                )

                // Initial Game Settings
                database.settingsDao().saveSettings(
                    GameSettingsEntity(
                        id = 1,
                        soundEffects = true,
                        music = true,
                        vibration = true,
                        notifications = true
                    )
                )
            }
        }
    }
}
