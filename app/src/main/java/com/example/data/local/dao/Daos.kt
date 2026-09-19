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
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET coins = coins + :coins, tickets = tickets + :tickets WHERE id = 1")
    suspend fun addCurrency(coins: Int, tickets: Int)

    @Query("UPDATE user_profile SET coins = coins - :coins WHERE id = 1 AND coins >= :coins")
    suspend fun deductCoins(coins: Int): Int

    @Query("UPDATE user_profile SET tickets = tickets - :tickets WHERE id = 1 AND tickets >= :tickets")
    suspend fun deductTickets(tickets: Int): Int

    @Query("UPDATE user_profile SET trophies = trophies + :trophiesDelta, totalMatches = totalMatches + 1, victories = victories + :victoryIncrement WHERE id = 1")
    suspend fun updateMatchOutcome(trophiesDelta: Int, victoryIncrement: Int)
}

@Dao
interface VipDao {
    @Query("SELECT * FROM vip_state WHERE id = 1")
    fun getVipState(): Flow<VipStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVip(vipState: VipStateEntity)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM reward_items")
    fun getAllRewards(): Flow<List<RewardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rewards: List<RewardItemEntity>)

    @Update
    suspend fun updateReward(reward: RewardItemEntity)

    @Query("UPDATE reward_items SET lastClaimedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordRewardClaim(id: String, timestamp: Long)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge_items")
    fun getAllChallenges(): Flow<List<ChallengeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<ChallengeItemEntity>)

    @Query("UPDATE challenge_items SET currentProgress = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, completed: Boolean)

    @Query("UPDATE challenge_items SET isClaimed = 1 WHERE id = :id")
    suspend fun markClaimed(id: String)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries ORDER BY rank ASC")
    fun getLeaderboard(): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardEntryEntity>)
}

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentMatches(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistoryEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<GameSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: GameSettingsEntity)
}
