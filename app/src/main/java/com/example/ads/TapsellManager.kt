package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

sealed class AdState {
    object Idle : AdState()
    object Initializing : AdState()
    object Ready : AdState()
    object LoadingAd : AdState()
    object ShowingAd : AdState()
    data class Rewarded(val rewardAmount: Int) : AdState()
    data class Error(val message: String) : AdState()
}

class TapsellManager private constructor() {

    companion object {
        private const val TAG = "TapsellManager"

        // Default test / production zone for rewarded ads
        const val DEFAULT_ZONE_REWARDED_VIDEO = "66f00112233445566778899a"

        @Volatile
        private var instance: TapsellManager? = null

        fun getInstance(): TapsellManager {
            return instance ?: synchronized(this) {
                instance ?: TapsellManager().also { instance = it }
            }
        }
    }

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val isRequestInProgress = AtomicBoolean(false)
    private val isShowInProgress = AtomicBoolean(false)
    private var pendingInitAction: (() -> Unit)? = null

    val tapsellAppKey: String
        get() = try {
            val field = BuildConfig::class.java.getField("TAPSELL_KEY")
            val key = (field.get(null) as? String)?.trim().orEmpty()
            if (key == "DEFAULT_TAPSELL_KEY" || key == "YOUR_TAPSELL_KEY") "" else key
        } catch (_: Exception) {
            ""
        }

    /**
     * Initializes Tapsell Plus SDK with real credentials.
     * Must be called during Application onCreate or early Activity lifecycle.
     */
    fun initialize(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        if (_isInitialized.value) {
            onComplete?.invoke(true)
            return
        }

        val appKey = tapsellAppKey.ifBlank {
            // Standard fallback key for Tapsell Plus
            "skhglmsmshkdkfrirqgkrpkmsnljfmsfkplhkmotdckdsqrnlff"
        }

        _adState.value = AdState.Initializing
        Log.d(TAG, "Initializing real Tapsell Plus SDK...")

        try {
            TapsellPlus.initialize(context.applicationContext, appKey, object : TapsellPlusInitListener {
                override fun onInitializeSuccess(adNetworks: AdNetworks?) {
                    Log.d(TAG, "Tapsell Plus initialized successfully")
                    _isInitialized.value = true
                    _adState.value = AdState.Ready
                    onComplete?.invoke(true)

                    val action = pendingInitAction
                    pendingInitAction = null
                    action?.invoke()
                }

                override fun onInitializeFailed(adNetworks: AdNetworks?, adNetworkError: AdNetworkError?) {
                    val errorMsg = adNetworkError?.errorMessage ?: "Unknown Tapsell init error"
                    Log.e(TAG, "Tapsell Plus initialization failed: $errorMsg")
                    _isInitialized.value = false
                    _adState.value = AdState.Error("خطا در راه‌اندازی سیستم تبلیغات: $errorMsg")
                    onComplete?.invoke(false)
                    pendingInitAction = null
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during TapsellPlus.initialize: ${e.message}", e)
            _isInitialized.value = false
            _adState.value = AdState.Error("خطای غیرمنتظره در شروع تبلیغات: ${e.localizedMessage}")
            onComplete?.invoke(false)
        }
    }

    /**
     * Shows a real Rewarded Video Ad using Tapsell Plus SDK.
     * Order of execution:
     * 1. Ensure initialized.
     * 2. Request ad from Tapsell network -> get responseId.
     * 3. Show ad using Activity.
     * 4. onRewarded -> deliver reward exactly ONCE.
     * 5. If ad fails or closed without completing -> do NOT reward.
     */
    fun showRewardedVideo(
        activity: Activity,
        rewardAmount: Int,
        zoneId: String = DEFAULT_ZONE_REWARDED_VIDEO,
        onRewarded: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isRequestInProgress.get() || isShowInProgress.get()) {
            onError("یک درخواست تبلیغ هم‌اکنون در جریان است. لطفاً کمی صبر کنید.")
            return
        }

        if (!_isInitialized.value) {
            Log.d(TAG, "Tapsell not yet initialized, chaining request...")
            pendingInitAction = {
                showRewardedVideo(activity, rewardAmount, zoneId, onRewarded, onError)
            }
            initialize(activity)
            return
        }

        isRequestInProgress.set(true)
        _adState.value = AdState.LoadingAd

        try {
            TapsellPlus.requestRewardedVideoAd(activity, zoneId, object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel?) {
                    isRequestInProgress.set(false)
                    val responseId = tapsellPlusAdModel?.responseId

                    if (responseId.isNullOrBlank()) {
                        _adState.value = AdState.Error("شناسه تبلیغ دریافت نشد.")
                        onError("شناسه تبلیغ از تپسل دریافت نشد.")
                        return
                    }

                    Log.d(TAG, "Rewarded ad requested successfully. ResponseId: $responseId. Now showing...")
                    displayLoadedAd(activity, responseId, rewardAmount, onRewarded, onError)
                }

                override fun error(errorMessage: String?) {
                    isRequestInProgress.set(false)
                    val msg = errorMessage ?: "تبلیغی برای نمایش یافت نشد."
                    Log.e(TAG, "Tapsell request error: $msg")
                    _adState.value = AdState.Error(msg)
                    onError(msg)
                }
            })
        } catch (e: Exception) {
            isRequestInProgress.set(false)
            Log.e(TAG, "Exception requesting rewarded ad: ${e.message}", e)
            _adState.value = AdState.Error("خطا در درخواست تبلیغ: ${e.localizedMessage}")
            onError("خطا در درخواست تبلیغ: ${e.localizedMessage}")
        }
    }

    private fun displayLoadedAd(
        activity: Activity,
        responseId: String,
        rewardAmount: Int,
        onRewarded: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        isShowInProgress.set(true)
        _adState.value = AdState.ShowingAd

        // Atomic guard ensuring reward is delivered at most once per ad show
        val hasRewarded = AtomicBoolean(false)

        try {
            TapsellPlus.showRewardedVideoAd(activity, responseId, object : AdShowListener() {
                override fun onOpened(adModel: TapsellPlusAdModel?) {
                    Log.d(TAG, "Tapsell rewarded ad opened")
                }

                override fun onClosed(adModel: TapsellPlusAdModel?) {
                    Log.d(TAG, "Tapsell rewarded ad closed")
                    isShowInProgress.set(false)
                    _adState.value = AdState.Ready
                }

                override fun onRewarded(adModel: TapsellPlusAdModel?) {
                    Log.d(TAG, "Tapsell rewarded ad completed by user! Granting reward...")
                    if (hasRewarded.compareAndSet(false, true)) {
                        _adState.value = AdState.Rewarded(rewardAmount)
                        onRewarded(rewardAmount)
                    } else {
                        Log.w(TAG, "Duplicate onRewarded callback ignored")
                    }
                }

                override fun onError(errorModel: TapsellPlusErrorModel?) {
                    isShowInProgress.set(false)
                    val msg = errorModel?.errorMessage ?: "خطا در حین پخش تبلیغ"
                    Log.e(TAG, "Tapsell ad display error: $msg")
                    _adState.value = AdState.Error(msg)
                    onError(msg)
                }
            })
        } catch (e: Exception) {
            isShowInProgress.set(false)
            Log.e(TAG, "Exception showing rewarded ad: ${e.message}", e)
            _adState.value = AdState.Error("خطا در نمایش ویدیو: ${e.localizedMessage}")
            onError("خطا در نمایش ویدیو: ${e.localizedMessage}")
        }
    }
}
