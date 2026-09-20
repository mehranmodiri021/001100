package com.arenaclash.game.data.repository

import com.arenaclash.game.data.local.dao.ChallengeDao
import com.arenaclash.game.data.local.dao.LeaderboardDao
import com.arenaclash.game.data.local.dao.MatchHistoryDao
import com.arenaclash.game.data.local.dao.RewardDao
import com.arenaclash.game.data.local.dao.SettingsDao
import com.arenaclash.game.data.local.entity.ChallengeItemEntity
import com.arenaclash.game.data.local.entity.GameSettingsEntity
import com.arenaclash.game.data.local.entity.LeaderboardEntryEntity
import com.arenaclash.game.data.local.entity.MatchHistoryEntity
import com.arenaclash.game.data.local.entity.RewardItemEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val challengeDao: ChallengeDao,
    private val rewardDao: RewardDao,
    private val leaderboardDao: LeaderboardDao,
    private val matchHistoryDao: MatchHistoryDao,
    private val settingsDao: SettingsDao
) {
    val challenges: Flow<List<ChallengeItemEntity>> = challengeDao.getAllChallenges()
    val rewards: Flow<List<RewardItemEntity>> = rewardDao.getAllRewards()
    val leaderboard: Flow<List<LeaderboardEntryEntity>> = leaderboardDao.getLeaderboard()
    val matchHistory: Flow<List<MatchHistoryEntity>> = matchHistoryDao.getRecentMatches()
    val settings: Flow<GameSettingsEntity?> = settingsDao.getSettings()

    suspend fun claimReward(rewardId: String) {
        rewardDao.recordRewardClaim(rewardId, System.currentTimeMillis())
    }

    suspend fun updateChallengeProgress(id: String, progress: Int, completed: Boolean) {
        challengeDao.updateProgress(id, progress, completed)
    }

    suspend fun claimChallengeReward(id: String) {
        challengeDao.markClaimed(id)
    }

    suspend fun recordMatch(match: MatchHistoryEntity) {
        matchHistoryDao.insertMatch(match)
    }

    suspend fun updateSettings(settings: GameSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }
}
