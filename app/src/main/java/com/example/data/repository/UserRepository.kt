package com.example.data.repository

import android.util.Log
import com.example.billing.BazaarConfig
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.VipDao
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.VipStateEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val vipDao: VipDao
) {
    companion object {
        private const val TAG = "UserRepository"
        const val DURATION_MONTHLY_MS = 30L * 24 * 60 * 60 * 1000L // 30 days
        const val DURATION_YEARLY_MS = 365L * 24 * 60 * 60 * 1000L // 365 days
    }

    val userProfileFlow: Flow<UserProfileEntity?> = userDao.getUserProfileFlow()
    val vipStateFlow: Flow<VipStateEntity?> = vipDao.getVipStateFlow()

    suspend fun getUserProfile(): UserProfileEntity {
        return userDao.getUserProfile() ?: UserProfileEntity().also {
            userDao.insertOrUpdateProfile(it)
        }
    }

    suspend fun getVipState(): VipStateEntity {
        val state = vipDao.getVipState() ?: VipStateEntity().also {
            vipDao.setVipState(it)
        }
        // Check if expired
        if (state.isVip && state.expiresAt <= System.currentTimeMillis()) {
            val expired = state.copy(isVip = false)
            vipDao.setVipState(expired)
            return expired
        }
        return state
    }

    /**
     * Activates VIP status using a cryptographically verified purchase from Cafe Bazaar.
     * Rejects empty or fake tokens ("verified", "restored_owned").
     */
    suspend fun activateVipWithToken(
        productId: String,
        purchaseToken: String
    ): Boolean {
        if (purchaseToken.isBlank() || purchaseToken == "verified" || purchaseToken == "restored_owned") {
            Log.e(TAG, "Rejected VIP activation: Fake or empty purchaseToken: '$purchaseToken'")
            return false
        }

        val durationMs = when (productId) {
            BazaarConfig.PRODUCT_VIP_MONTHLY -> DURATION_MONTHLY_MS
            BazaarConfig.PRODUCT_VIP_YEARLY -> DURATION_YEARLY_MS
            else -> {
                Log.e(TAG, "Product $productId is not a valid VIP subscription product!")
                return false
            }
        }

        val now = System.currentTimeMillis()
        val currentVip = vipDao.getVipState()
        // If already VIP and not expired, extend from existing expiry; otherwise from now
        val baseTime = if (currentVip != null && currentVip.isVip && currentVip.expiresAt > now) {
            currentVip.expiresAt
        } else {
            now
        }
        val expiresAt = baseTime + durationMs

        val planType = if (productId == BazaarConfig.PRODUCT_VIP_MONTHLY) "monthly" else "yearly"

        val updatedVip = VipStateEntity(
            id = 1,
            isVip = true,
            planType = planType,
            purchaseToken = purchaseToken,
            activatedAt = now,
            expiresAt = expiresAt
        )
        vipDao.setVipState(updatedVip)
        Log.d(TAG, "VIP successfully activated until $expiresAt with token $purchaseToken")
        return true
    }

    suspend fun addCoins(amount: Int) {
        if (amount > 0) {
            userDao.addCoins(amount)
        }
    }

    suspend fun addTickets(amount: Int) {
        if (amount > 0) {
            userDao.addTickets(amount)
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val profile = getUserProfile()
        if (profile.coins >= amount) {
            userDao.addCoins(-amount)
            return true
        }
        return false
    }

    suspend fun spendTicket(): Boolean {
        val profile = getUserProfile()
        if (profile.tickets >= 1) {
            userDao.addTickets(-1)
            return true
        }
        return false
    }

    suspend fun updateTrophies(delta: Int) {
        userDao.updateTrophies(delta)
    }
}
