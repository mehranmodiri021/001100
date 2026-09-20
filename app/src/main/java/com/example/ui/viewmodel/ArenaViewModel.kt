package com.arenaclash.game.ui.viewmodel

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arenaclash.game.ads.AdState
import com.arenaclash.game.ads.TapsellManager
import com.arenaclash.game.billing.BazaarBillingManager
import com.arenaclash.game.billing.BazaarConfig
import com.arenaclash.game.billing.PurchaseResult
import com.arenaclash.game.billing.VerifiedPurchase
import com.arenaclash.game.data.local.entity.ChallengeItemEntity
import com.arenaclash.game.data.local.entity.GameSettingsEntity
import com.arenaclash.game.data.local.entity.LeaderboardEntryEntity
import com.arenaclash.game.data.local.entity.MatchHistoryEntity
import com.arenaclash.game.data.local.entity.RewardItemEntity
import com.arenaclash.game.data.local.entity.UserProfileEntity
import com.arenaclash.game.data.local.entity.VipStateEntity
import com.arenaclash.game.data.repository.GameRepository
import com.arenaclash.game.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class BattleFinished(val isVictory: Boolean, val coinsEarned: Int, val trophiesDelta: Int) : UiEvent()
}

class ArenaViewModel(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    val billingManager: BazaarBillingManager
) : ViewModel() {

    val userProfile: StateFlow<UserProfileEntity?> = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vipState: StateFlow<VipStateEntity?> = userRepository.vipState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val challenges: StateFlow<List<ChallengeItemEntity>> = gameRepository.challenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewards: StateFlow<List<RewardItemEntity>> = gameRepository.rewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntryEntity>> = gameRepository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = gameRepository.matchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<GameSettingsEntity?> = gameRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // TapsellManager یک object است، پس مستقیم به adState دسترسی داریم
    val adState: StateFlow<AdState> = TapsellManager.adState

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    init {
        observeBillingEvents()
    }

    private fun observeBillingEvents() {
        viewModelScope.launch {
            billingManager.purchaseEvents.collect { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        handleVerifiedPurchase(result.purchase)
                    }
                    is PurchaseResult.Error -> {
                        _uiEvents.emit(UiEvent.ShowSnackbar("خطای پرداخت: ${result.message}"))
                    }
                    is PurchaseResult.VerificationFailed -> {
                        _uiEvents.emit(UiEvent.ShowSnackbar("خطای امنیتی: ${result.reason}"))
                    }
                    is PurchaseResult.UserCancelled -> {
                        _uiEvents.emit(UiEvent.ShowSnackbar("خرید توسط کاربر لغو گردید."))
                    }
                }
            }
        }
    }

    private suspend fun handleVerifiedPurchase(purchase: VerifiedPurchase) {
        when (purchase.productId) {
            BazaarConfig.PRODUCT_VIP_MONTHLY -> {
                userRepository.activateVip(30, "monthly", purchase.purchaseToken)
                _uiEvents.emit(UiEvent.ShowSnackbar("اشتراک VIP ماهانه با موفقیت فعال شد!"))
            }
            BazaarConfig.PRODUCT_VIP_YEARLY -> {
                userRepository.activateVip(365, "yearly", purchase.purchaseToken)
                _uiEvents.emit(UiEvent.ShowSnackbar("اشتراک طلایی سالانه VIP با موفقیت فعال شد!"))
            }
            BazaarConfig.PRODUCT_COINS_1000 -> {
                userRepository.addCurrency(1000, 0)
                billingManager.consumePurchase(purchase.purchaseToken)
                _uiEvents.emit(UiEvent.ShowSnackbar("بسته ۱۰۰۰ سکه به حساب شما اضافه شد!"))
            }
            BazaarConfig.PRODUCT_COINS_5000 -> {
                userRepository.addCurrency(5000, 0)
                billingManager.consumePurchase(purchase.purchaseToken)
                _uiEvents.emit(UiEvent.ShowSnackbar("بسته ۵۰۰۰ سکه به حساب شما اضافه شد!"))
            }
            BazaarConfig.PRODUCT_TICKETS_10 -> {
                userRepository.addCurrency(0, 10)
                billingManager.consumePurchase(purchase.purchaseToken)
                _uiEvents.emit(UiEvent.ShowSnackbar("۱۰ بلیط ورود به آرنا به موجودی شما افزوده شد!"))
            }
            else -> {
                _uiEvents.emit(UiEvent.ShowSnackbar("خرید محصول ${purchase.productId} تایید شد."))
            }
        }
    }

    fun buyProduct(
        activityLauncher: ActivityResultLauncher<IntentSenderRequest>,
        productId: String
    ) {
        billingManager.launchPurchaseFlow(activityLauncher, productId)
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowSnackbar("در حال استعلام و بازیابی خریدهای پیشین از کافه بازار..."))
            billingManager.queryPurchases { purchases ->
                if (purchases.isEmpty()) {
                    viewModelScope.launch {
                        _uiEvents.emit(UiEvent.ShowSnackbar("هیچ خرید فعالی در حساب بازار شما یافت نشد."))
                    }
                    return@queryPurchases
                }

                viewModelScope.launch {
                    var restoredCount = 0
                    purchases.forEach { purchase ->
                        if (purchase.productId == BazaarConfig.PRODUCT_VIP_MONTHLY) {
                            userRepository.activateVip(30, "monthly", purchase.purchaseToken)
                            restoredCount++
                        } else if (purchase.productId == BazaarConfig.PRODUCT_VIP_YEARLY) {
                            userRepository.activateVip(365, "yearly", purchase.purchaseToken)
                            restoredCount++
                        }
                    }

                    if (restoredCount > 0) {
                        _uiEvents.emit(UiEvent.ShowSnackbar("اشتراک VIP شما با موفقیت بازیابی شد."))
                    } else {
                        _uiEvents.emit(UiEvent.ShowSnackbar("خریدهای پیشین بررسی شد؛ اشتراک فعالی یافت نشد."))
                    }
                }
            }
        }
    }

    fun claimDailyReward(reward: RewardItemEntity) {
        viewModelScope.launch {
            if (!reward.isAvailable) {
                _uiEvents.emit(UiEvent.ShowSnackbar("این هدیه هنوز آماده دریافت نیست."))
                return@launch
            }
            gameRepository.claimReward(reward.id)
            userRepository.addCurrency(reward.rewardCoins, reward.rewardTickets)
            _uiEvents.emit(UiEvent.ShowSnackbar("هدیه دریافت شد: +${reward.rewardCoins} سکه، +${reward.rewardTickets} بلیط"))
        }
    }

    fun watchRewardedAd(activity: Activity) {
        TapsellManager.requestAndShowRewardedVideo(
            activity = activity,
            rewardCoins = 150,
            onRewarded = { coins ->
                viewModelScope.launch {
                    userRepository.addCurrency(coins, 0)
                    _uiEvents.emit(UiEvent.ShowSnackbar("تبریک! $coins سکه به موجودی شما افزوده شد."))
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowSnackbar("خطای تبلیغات: $error"))
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity) {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowSnackbar("در حال آماده‌سازی ویدیوی فوری..."))
        }
        TapsellManager.requestAndShowInterstitial(
            activity = activity,
            onClosed = {
                viewModelScope.launch {
                    userRepository.addCurrency(50, 0)
                    _uiEvents.emit(UiEvent.ShowSnackbar("پاداش تبلیغ فوری دریافت شد: +۵۰ سکه طلا"))
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowSnackbar("خطا در پخش ویدیو فوری: $error"))
                }
            }
        )
    }

    fun startBattle(arenaName: String, ticketCost: Int) {
        viewModelScope.launch {
            val hasTicket = userRepository.deductTickets(ticketCost)
            if (!hasTicket) {
                _uiEvents.emit(UiEvent.ShowSnackbar("بلیط کافی برای ورود به $arenaName ندارید!"))
                return@launch
            }

            val opponents = listOf("سردار آتش", "تندر سیاه", "گرگ صحرا", "عقاب البرز", "تکاور زاگرس")
            val opponent = opponents.random()
            val isVictory = (1..100).random() <= 65

            val coinsEarned = if (isVictory) 120 else 25
            val trophiesDelta = if (isVictory) 30 else -15

            userRepository.addCurrency(coinsEarned, 0)
            userRepository.recordMatchResult(trophiesDelta, isVictory)

            gameRepository.recordMatch(
                MatchHistoryEntity(
                    opponentName = opponent,
                    isVictory = isVictory,
                    arenaName = arenaName,
                    coinsDelta = coinsEarned,
                    trophiesDelta = trophiesDelta
                )
            )

            // Update real challenge progress based on actual profile statistics
            val updatedProfile = userRepository.getProfileSnapshot()
            if (updatedProfile != null) {
                val ch1 = updatedProfile.victories.coerceAtMost(3)
                gameRepository.updateChallengeProgress("ch_1", ch1, ch1 >= 3)

                val ch2 = updatedProfile.totalMatches.coerceAtMost(5)
                gameRepository.updateChallengeProgress("ch_2", ch2, ch2 >= 5)

                val ch3 = updatedProfile.totalMatches.coerceAtMost(10)
                gameRepository.updateChallengeProgress("ch_3", ch3, ch3 >= 10)

                val ch4 = updatedProfile.trophies.coerceAtMost(2000)
                gameRepository.updateChallengeProgress("ch_4", ch4, ch4 >= 2000)
            }

            _uiEvents.emit(UiEvent.BattleFinished(isVictory, coinsEarned, trophiesDelta))
        }
    }

    fun claimChallenge(challenge: ChallengeItemEntity) {
        viewModelScope.launch {
            if (!challenge.isCompleted || challenge.isClaimed) return@launch
            gameRepository.claimChallengeReward(challenge.id)
            userRepository.addCurrency(challenge.rewardCoins, 0)
            _uiEvents.emit(UiEvent.ShowSnackbar("پاداش چالش دریافت شد: +${challenge.rewardCoins} سکه"))
        }
    }

    fun updateSettings(settings: GameSettingsEntity) {
        viewModelScope.launch {
            gameRepository.updateSettings(settings)
        }
    }
}

class ArenaViewModelFactory(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    private val billingManager: BazaarBillingManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArenaViewModel::class.java)) {
            return ArenaViewModel(
                userRepository,
                gameRepository,
                billingManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
