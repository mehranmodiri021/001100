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
    version = 2,
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
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                database.userDao().insertOrUpdateProfile(UserProfileEntity())
                database.vipDao().insertOrUpdateVip(VipStateEntity())
                database.settingsDao().insertOrUpdate(GameSettingsEntity())

                database.rewardDao().insertAll(
                    listOf(
                        RewardItemEntity("daily_chest", "صندوق روزانه", "هر ۲۴ ساعت یکبار سکه و بلیط رایگان دریافت کنید", 250, 2, 1440),
                        RewardItemEntity("bronze_chest", "صندوق برنزی", "صندوق آماده بازگشایی هر ۴ ساعت", 100, 1, 240),
                        RewardItemEntity("video_chest", "صندوق تماشای ویدیو", "با تماشای ویدیوی حامی تپسل سکه دریافت کنید", 150, 0, 0)
                    )
                )

                // Clean initialization: All challenges start strictly from 0 progress
                database.challengeDao().insertAll(
                    listOf(
                        ChallengeItemEntity("ch_1", "شکارچی آرنا", "در ۳ نبرد آرنا پیروز شوید", 3, 0, 300, 150),
                        ChallengeItemEntity("ch_2", "مبارز خستگی‌ناپذیر", "در ۵ نبرد شرکت کنید", 5, 0, 200, 100),
                        ChallengeItemEntity("ch_3", "استاد ضربات مهلک", "در ۱۰ نبرد آرنا شرکت کنید", 10, 0, 400, 250),
                        ChallengeItemEntity("ch_4", "فاتح طلایی", "به کاپ ۲۰۰۰ برسید", 2000, 0, 1000, 500)
                    )
                )

                database.leaderboardDao().insertAll(
                    listOf(
                        LeaderboardEntryEntity(1, "شیر خاورمیانه", 3250, 145, isVip = true),
                        LeaderboardEntryEntity(2, "رستم دستان", 2980, 120, isVip = true),
                        LeaderboardEntryEntity(3, "تندر سیاه", 2740, 98, isVip = false),
                        LeaderboardEntryEntity(4, "ققنوس آرنا", 2420, 85, isVip = true),
                        LeaderboardEntryEntity(5, "سردار آتش", 1850, 44, isVip = false),
                        LeaderboardEntryEntity(6, "شبح صحرا", 1680, 38, isVip = false),
                        LeaderboardEntryEntity(7, "گرگ زاگرس", 1420, 31, isVip = false),
                        LeaderboardEntryEntity(8, "جنگجوی آرنا (شما)", 0, 0, isVip = false, isCurrentUser = true)
                    )
                )

                // A clean installation must not have pre-seeded fake match history
            }
        }
    }
}
