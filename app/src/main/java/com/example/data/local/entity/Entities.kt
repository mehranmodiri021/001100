package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "جنگجوی آرنا",
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 500,
    val coins: Int = 250,
    val tickets: Int = 5,
    val trophies: Int = 0,
    val totalMatches: Int = 0,
    val victories: Int = 0
)

@Entity(tableName = "vip_state")
data class VipStateEntity(
    @PrimaryKey val id: Int = 1,
    val isVip: Boolean = false,
    val expiresAt: Long = 0L,
    val tier: String = "free",
    val purchaseToken: String? = null
) {
    val isActive: Boolean
        get() = isVip && expiresAt > System.currentTimeMillis()
}

@Entity(tableName = "reward_items")
data class RewardItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val rewardTickets: Int,
    val cooldownMinutes: Int,
    val lastClaimedTimestamp: Long = 0L
) {
    val isAvailable: Boolean
        get() {
            if (lastClaimedTimestamp == 0L) return true
            val elapsedMillis = System.currentTimeMillis() - lastClaimedTimestamp
            return elapsedMillis >= (cooldownMinutes * 60 * 1000L)
        }
}

@Entity(tableName = "challenge_items")
data class ChallengeItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentProgress: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey val rank: Int,
    val username: String,
    val trophies: Int,
    val victories: Int,
    val isVip: Boolean,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val matchId: Long = 0,
    val opponentName: String,
    val isVictory: Boolean,
    val arenaName: String,
    val coinsDelta: Int,
    val trophiesDelta: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEffects: Boolean = true,
    val music: Boolean = true,
    val vibration: Boolean = true,
    val notifications: Boolean = true
)
