package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "قهرمان آرنا",
    val coins: Int = 1500,
    val tickets: Int = 5,
    val trophies: Int = 420,
    val level: Int = 3,
    val xp: Int = 240,
    val avatarId: Int = 1
)

@Entity(tableName = "vip_state")
data class VipStateEntity(
    @PrimaryKey val id: Int = 1,
    val isVip: Boolean = false,
    val planType: String = "", // "monthly" or "yearly"
    val purchaseToken: String = "",
    val activatedAt: Long = 0L,
    val expiresAt: Long = 0L
) {
    val isActive: Boolean
        get() = isVip && expiresAt > System.currentTimeMillis()
}

@Entity(tableName = "reward_items")
data class RewardItemEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val rewardType: String, // "coins", "tickets", "chest"
    val amount: Int,
    val isClaimed: Boolean = false,
    val cooldownMinutes: Int = 0,
    val lastClaimedAt: Long = 0L
)

@Entity(tableName = "challenges")
data class ChallengeItemEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val rewardCoins: Int,
    val rewardXp: Int,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntryEntity(
    @PrimaryKey val id: Int,
    val rank: Int,
    val username: String,
    val trophies: Int,
    val isCurrentUser: Boolean = false,
    val isVip: Boolean = false
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opponentName: String,
    val isVictory: Boolean,
    val playerScore: Int,
    val opponentScore: Int,
    val coinsEarned: Int,
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
