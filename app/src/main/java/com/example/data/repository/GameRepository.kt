package com.example.data.repository

import com.example.data.local.dao.ChallengeDao
import com.example.data.local.dao.LeaderboardDao
import com.example.data.local.dao.MatchHistoryDao
import com.example.data.local.dao.RewardDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.entity.ChallengeItemEntity
import com.example.data.local.entity.GameSettingsEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.MatchHistoryEntity
import com.example.data.local.entity.RewardItemEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val challengeDao: ChallengeDao,
    private val rewardDao: RewardDao,
    private val leaderboardDao: LeaderboardDao,
    private val matchHistoryDao: MatchHistoryDao,
    private val settingsDao: SettingsDao
) {
    val challengesFlow: Flow<List<ChallengeItemEntity>> = challengeDao.getAllChallengesFlow()
    val rewardsFlow: Flow<List<RewardItemEntity>> = rewardDao.getAllRewardsFlow()
    val leaderboardFlow: Flow<List<LeaderboardEntryEntity>> = leaderboardDao.getLeaderboardFlow()
    val matchHistoryFlow: Flow<List<MatchHistoryEntity>> = matchHistoryDao.getRecentMatchesFlow()
    val settingsFlow: Flow<GameSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun claimReward(reward: RewardItemEntity): Boolean {
        if (reward.isClaimed) return false
        val updated = reward.copy(isClaimed = true, lastClaimedAt = System.currentTimeMillis())
        rewardDao.updateReward(updated)
        return true
    }

    suspend fun claimChallengeReward(challenge: ChallengeItemEntity): Boolean {
        if (!challenge.isCompleted || challenge.isRewardClaimed) return false
        val updated = challenge.copy(isRewardClaimed = true)
        challengeDao.updateChallenge(updated)
        return true
    }

    suspend fun progressChallenge(challengeId: Int, increment: Int = 1) {
        // Will be updated if present
    }

    suspend fun recordMatch(
        opponentName: String,
        isVictory: Boolean,
        playerScore: Int,
        opponentScore: Int,
        coinsEarned: Int,
        trophiesDelta: Int
    ) {
        val match = MatchHistoryEntity(
            opponentName = opponentName,
            isVictory = isVictory,
            playerScore = playerScore,
            opponentScore = opponentScore,
            coinsEarned = coinsEarned,
            trophiesDelta = trophiesDelta
        )
        matchHistoryDao.insertMatch(match)
    }

    suspend fun updateSettings(settings: GameSettingsEntity) {
        settingsDao.saveSettings(settings)
    }
}
