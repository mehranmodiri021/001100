package com.arenaclash.game.data.repository

import com.arenaclash.game.data.local.dao.UserDao
import com.arenaclash.game.data.local.dao.VipDao
import com.arenaclash.game.data.local.entity.UserProfileEntity
import com.arenaclash.game.data.local.entity.VipStateEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val vipDao: VipDao
) {
    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfile()
    val vipState: Flow<VipStateEntity?> = vipDao.getVipState()

    suspend fun addCurrency(coins: Int, tickets: Int) {
        userDao.addCurrency(coins, tickets)
    }

    suspend fun deductCoins(coins: Int): Boolean {
        return userDao.deductCoins(coins) > 0
    }

    suspend fun deductTickets(tickets: Int): Boolean {
        return userDao.deductTickets(tickets) > 0
    }

    suspend fun getProfileSnapshot(): UserProfileEntity? {
        return userDao.getProfileSnapshot()
    }

    suspend fun recordMatchResult(trophiesDelta: Int, isVictory: Boolean) {
        val victoryIncrement = if (isVictory) 1 else 0
        userDao.updateMatchOutcome(trophiesDelta, victoryIncrement)
    }

    suspend fun activateVip(durationDays: Int, tier: String, purchaseToken: String?) {
        val currentExpiry = System.currentTimeMillis()
        val additionalMillis = durationDays * 24L * 60 * 60 * 1000L
        val newExpiry = currentExpiry + additionalMillis
        val vipEntity = VipStateEntity(
            id = 1,
            isVip = true,
            expiresAt = newExpiry,
            tier = tier,
            purchaseToken = purchaseToken
        )
        vipDao.insertOrUpdateVip(vipEntity)
    }
}
