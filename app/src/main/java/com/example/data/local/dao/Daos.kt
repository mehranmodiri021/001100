package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChallengeItemEntity
import com.example.data.local.entity.GameSettingsEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.MatchHistoryEntity
import com.example.data.local.entity.RewardItemEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.VipStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET coins = coins + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("UPDATE user_profile SET tickets = tickets + :amount WHERE id = 1")
    suspend fun addTickets(amount: Int)

    @Query("UPDATE user_profile SET trophies = trophies + :delta WHERE id = 1")
    suspend fun updateTrophies(delta: Int)
}

@Dao
interface VipDao {
    @Query("SELECT * FROM vip_state WHERE id = 1")
    fun getVipStateFlow(): Flow<VipStateEntity?>

    @Query("SELECT * FROM vip_state WHERE id = 1")
    suspend fun getVipState(): VipStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setVipState(vipState: VipStateEntity)

    @Query("UPDATE vip_state SET isVip = 0 WHERE id = 1 AND expiresAt < :currentTime")
    suspend fun checkAndExpireVip(currentTime: Long)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM reward_items ORDER BY id ASC")
    fun getAllRewardsFlow(): Flow<List<RewardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardItemEntity>)

    @Update
    suspend fun updateReward(reward: RewardItemEntity)

    @Query("SELECT COUNT(*) FROM reward_items")
    suspend fun countRewards(): Int
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY id ASC")
    fun getAllChallengesFlow(): Flow<List<ChallengeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeItemEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeItemEntity)

    @Query("SELECT COUNT(*) FROM challenges")
    suspend fun countChallenges(): Int
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    fun getLeaderboardFlow(): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntryEntity>)

    @Query("SELECT COUNT(*) FROM leaderboard")
    suspend fun countEntries(): Int
}

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentMatchesFlow(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistoryEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<GameSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettingsEntity)
}
