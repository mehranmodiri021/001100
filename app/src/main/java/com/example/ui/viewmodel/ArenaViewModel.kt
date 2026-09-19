package com.example.ui.viewmodel

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ads.TapsellManager
import com.example.billing.BazaarBillingManager
import com.example.billing.BazaarConfig
import com.example.billing.PurchaseResult
import com.example.data.local.entity.ChallengeItemEntity
import com.example.data.local.entity.GameSettingsEntity
import com.example.data.local.entity.LeaderboardEntryEntity
import com.example.data.local.entity.MatchHistoryEntity
import com.example.data.local.entity.RewardItemEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.VipStateEntity
import com.example.data.repository.GameRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class BattleFinished(val isVictory: Boolean, val coinsEarned: Int, val trophiesDelta: Int) : UiEvent()
}

class ArenaViewModel(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    val billingManager: BazaarBillingManager,
    val tapsellManager: TapsellManager
) : ViewModel() {

    val userProfile: StateFlow<UserProfileEntity?> = userRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vipState: StateFlow<VipStateEntity?> = userRepository.vipStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val challenges: StateFlow<List<ChallengeItemEntity>> = gameRepository.challengesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewards: StateFlow<List<RewardItemEntity>> = gameRepository.rewardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntryEntity>> = gameRepository.leaderboardFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = gameRepository.matchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<GameSettingsEntity?> = gameRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Listen to verified purchase events from BazaarBillingManager
        viewModelScope.launch {
            billingManager.purchaseEvents.collect { result ->
                handlePurchaseResult(result)
            }
        }
    }

    private fun handlePurchaseResult(result: PurchaseResult) {
        viewModelScope.launch {
            when (result) {
                is PurchaseResult.Success -> {
                    // Only genuine verified purchase tokens are processed here
                    when (result.productId) {
                        BazaarConfig.PRODUCT_VIP_MONTHLY, BazaarConfig.PRODUCT_VIP_YEARLY -> {
                            val activated = userRepository.activateVipWithToken(
                                productId = result.productId,
                                purchaseToken = result.purchaseToken
                            )
                            if (activated) {
                                val durationText = if (result.productId == BazaarConfig.PRODUCT_VIP_MONTHLY) "۳۰ روزه" else "یک ساله"
                                _uiEvents.emit(UiEvent.ShowSnackbar("اشتراک VIP $durationText با موفقیت فعال شد!"))
                            } else {
                                _uiEvents.emit(UiEvent.ShowSnackbar("خطا در فعال‌سازی VIP: توکن خرید نامعتبر است."))
                            }
                        }
                        BazaarConfig.PRODUCT_COINS_1000 -> {
                            userRepository.addCoins(1000)
                            billingManager.consumePurchase(result.purchaseToken)
                            _uiEvents.emit(UiEvent.ShowSnackbar("۱,۰۰۰ سکه با موفقیت به حساب شما اضافه شد!"))
                        }
                        BazaarConfig.PRODUCT_COINS_5000 -> {
                            userRepository.addCoins(5000)
                            billingManager.consumePurchase(result.purchaseToken)
                            _uiEvents.emit(UiEvent.ShowSnackbar("۵,۰۰۰ سکه طلایی با موفقیت دریافت شد!"))
                        }
                        BazaarConfig.PRODUCT_TICKETS_10 -> {
                            userRepository.addTickets(10)
                            billingManager.consumePurchase(result.purchaseToken)
                            _uiEvents.emit(UiEvent.ShowSnackbar("۱۰ بلیط نبرد طلایی به موجودی شما افزوده شد!"))
                        }
                        else -> {
                            _uiEvents.emit(UiEvent.ShowSnackbar("محصول خریداری شده شناسایی نشد."))
                        }
                    }
                }
                is PurchaseResult.Error -> {
                    _uiEvents.emit(UiEvent.ShowSnackbar(result.message))
                }
                is PurchaseResult.Canceled -> {
                    _uiEvents.emit(UiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    /**
     * Starts Bazaar purchase flow for the specified product.
     */
    fun purchaseProduct(launcher: ActivityResultLauncher<IntentSenderRequest>, productId: String) {
        billingManager.launchPurchase(launcher, productId)
    }

    /**
     * Restore purchases cryptographically verified from Bazaar.
     */
    fun restorePurchases() {
        viewModelScope.launch {
            _isLoading.value = true
            val verifiedPurchases = billingManager.restorePurchases()
            _isLoading.value = false

            if (verifiedPurchases.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowSnackbar("هیچ خرید یا اشتراک فعالی در حساب کافه بازار شما یافت نشد."))
                return@launch
            }

            var vipRestored = false
            for (purchase in verifiedPurchases) {
                if (purchase.productId == BazaarConfig.PRODUCT_VIP_MONTHLY ||
                    purchase.productId == BazaarConfig.PRODUCT_VIP_YEARLY
                ) {
                    val success = userRepository.activateVipWithToken(
                        productId = purchase.productId,
                        purchaseToken = purchase.purchaseToken
                    )
                    if (success) {
                        vipRestored = true
                    }
                }
            }

            if (vipRestored) {
                _uiEvents.emit(UiEvent.ShowSnackbar("اشتراک VIP شما با موفقیت بازیابی شد!"))
            } else {
                _uiEvents.emit(UiEvent.ShowSnackbar("${verifiedPurchases.size} خرید بازیابی شد."))
            }
        }
    }

    /**
     * Shows a real Rewarded Video Ad via Tapsell Plus.
     */
    fun watchRewardedAd(activity: Activity, rewardType: String = "coins") {
        val rewardAmount = if (rewardType == "coins") 150 else 2
        tapsellManager.showRewardedVideo(
            activity = activity,
            rewardAmount = rewardAmount,
            onRewarded = { grantedAmount ->
                viewModelScope.launch {
                    if (rewardType == "coins") {
                        userRepository.addCoins(grantedAmount)
                        _uiEvents.emit(UiEvent.ShowSnackbar("پاداش ویدیو: $grantedAmount سکه به حساب شما واریز شد!"))
                    } else {
                        userRepository.addTickets(grantedAmount)
                        _uiEvents.emit(UiEvent.ShowSnackbar("پاداش ویدیو: $grantedAmount بلیط نبرد دریافت کردید!"))
                    }
                }
            },
            onError = { errorMsg ->
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowSnackbar(errorMsg))
                }
            }
        )
    }

    /**
     * Simulates an Arena Battle:
     * Costs 1 ticket, awards coins and trophies on victory.
     */
    fun startBattle(arenaTierName: String, coinBet: Int) {
        viewModelScope.launch {
            val hasTicket = userRepository.spendTicket()
            if (!hasTicket) {
                _uiEvents.emit(UiEvent.ShowSnackbar("برای ورود به میدان نبرد نیاز به حداقل ۱ بلیط دارید!"))
                return@launch
            }

            _isLoading.value = true
            // Quick deterministic battle calculation based on user level and random RNG
            val isVictory = Random.nextInt(100) < 65
            val playerScore = if (isVictory) 3 else Random.nextInt(1, 3)
            val opponentScore = if (isVictory) Random.nextInt(0, 2) else 3
            val coinsEarned = if (isVictory) coinBet * 2 else 20
            val trophiesDelta = if (isVictory) 30 else -12

            val opponents = listOf("سهراب_شکارچی", "شیر_بابل", "طوفان_سرخ", "عقاب_زاگرس", "شوالیه_تاریک")
            val opponentName = opponents.random()

            if (isVictory) {
                userRepository.addCoins(coinsEarned)
            }
            userRepository.updateTrophies(trophiesDelta)

            gameRepository.recordMatch(
                opponentName = opponentName,
                isVictory = isVictory,
                playerScore = playerScore,
                opponentScore = opponentScore,
                coinsEarned = coinsEarned,
                trophiesDelta = trophiesDelta
            )

            _isLoading.value = false
            _uiEvents.emit(UiEvent.BattleFinished(isVictory, coinsEarned, trophiesDelta))
        }
    }

    fun claimDailyReward(reward: RewardItemEntity) {
        viewModelScope.launch {
            val claimed = gameRepository.claimReward(reward)
            if (claimed) {
                when (reward.rewardType) {
                    "coins" -> userRepository.addCoins(reward.amount)
                    "tickets" -> userRepository.addTickets(reward.amount)
                    "chest" -> {
                        userRepository.addCoins(reward.amount * 250)
                        userRepository.addTickets(2)
                    }
                }
                _uiEvents.emit(UiEvent.ShowSnackbar("جایزه '${reward.title}' با موفقیت دریافت شد!"))
            }
        }
    }

    fun claimChallengeReward(challenge: ChallengeItemEntity) {
        viewModelScope.launch {
            val claimed = gameRepository.claimChallengeReward(challenge)
            if (claimed) {
                userRepository.addCoins(challenge.rewardCoins)
                _uiEvents.emit(UiEvent.ShowSnackbar("پاداش چالش '${challenge.title}' دریافت شد (${challenge.rewardCoins} سکه)!"))
            }
        }
    }

    fun updateSettings(settings: GameSettingsEntity) {
        viewModelScope.launch {
            gameRepository.updateSettings(settings)
            _uiEvents.emit(UiEvent.ShowSnackbar("تنظیمات با موفقیت ذخیره شد."))
        }
    }
}

class ArenaViewModelFactory(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    private val billingManager: BazaarBillingManager,
    private val tapsellManager: TapsellManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArenaViewModel::class.java)) {
            return ArenaViewModel(userRepository, gameRepository, billingManager, tapsellManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
