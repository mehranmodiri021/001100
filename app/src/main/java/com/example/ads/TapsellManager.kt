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
    data object Idle : AdState()
    data object Loading : AdState()
    data class Ready(val responseId: String) : AdState()
    data object Showing : AdState()
    data class Rewarded(val rewardAmount: Int) : AdState()
    data class Error(val message: String) : AdState()
}

class TapsellManager private constructor() {

    companion object {
        private const val TAG = "TapsellManager"
        const val REWARDED_ZONE_ID = "arena_rewarded_video_zone"

        @Volatile
        private var INSTANCE: TapsellManager? = null

        fun getInstance(): TapsellManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TapsellManager().also { INSTANCE = it }
            }
        }
    }

    private var isInitialized = false
    private val isInitializing = AtomicBoolean(false)

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

    fun initialize(context: Context, onInitialized: ((Boolean) -> Unit)? = null) {
        if (isInitialized) {
            onInitialized?.invoke(true)
            return
        }

        val appKey = tapsellAppKey
        if (appKey.isBlank()) {
            Log.w(TAG, "Tapsell appKey is not configured. Tapsell SDK initialization skipped.")
            onInitialized?.invoke(false)
            return
        }

        if (!isInitializing.compareAndSet(false, true)) {
            Log.d(TAG, "Tapsell initialization already in progress.")
            return
        }

        TapsellPlus.initialize(context, appKey, object : TapsellPlusInitListener {
            override fun onInitializeSuccess(adNetworks: AdNetworks?) {
                isInitialized = true
                isInitializing.set(false)
                Log.d(TAG, "TapsellPlus initialized successfully.")
                onInitialized?.invoke(true)
                pendingInitAction?.invoke()
                pendingInitAction = null
            }

            override fun onInitializeFailed(adNetworks: AdNetworks?, adNetworkError: AdNetworkError?) {
                isInitialized = false
                isInitializing.set(false)
                val errMsg = adNetworkError?.errorMessage ?: "خطای ناشناخته در مقداردهی تپسل"
                Log.e(TAG, "TapsellPlus initialization failed: $errMsg")
                onInitialized?.invoke(false)
                pendingInitAction = null
            }
        })
    }

    fun requestRewardedVideo(activity: Activity, zoneId: String = REWARDED_ZONE_ID) {
        if (!isInitialized) {
            val appKey = tapsellAppKey
            if (appKey.isBlank()) {
                _adState.value = AdState.Error("کلید تپسل هنوز تنظیم نشده است.")
                return
            }
            pendingInitAction = { requestRewardedVideo(activity, zoneId) }
            initialize(activity)
            return
        }

        if (!isRequestInProgress.compareAndSet(false, true)) {
            Log.w(TAG, "Ad request already in progress. Skipping.")
            return
        }

        _adState.value = AdState.Loading

        TapsellPlus.requestRewardedVideoAd(activity, zoneId, object : AdRequestCallback() {
            override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                isRequestInProgress.set(false)
                val responseId = tapsellPlusAdModel.responseId
                Log.d(TAG, "Rewarded ad is ready with responseId: $responseId")
                _adState.value = AdState.Ready(responseId)
            }

            override fun error(errorMessage: String?) {
                isRequestInProgress.set(false)
                val msg = errorMessage ?: "خطا در دریافت ویدیو جایزه‌دار"
                Log.e(TAG, "Error requesting rewarded ad: $msg")
                _adState.value = AdState.Error(msg)
            }
        })
    }

    fun showRewardedVideo(
        activity: Activity,
        responseId: String,
        rewardCoins: Int = 100,
        onRewarded: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isShowInProgress.compareAndSet(false, true)) {
            Log.w(TAG, "Ad is already showing. Ignoring duplicate show call.")
            return
        }

        _adState.value = AdState.Showing
        val rewardDelivered = AtomicBoolean(false)

        TapsellPlus.showRewardedVideoAd(activity, responseId, object : AdShowListener() {
            override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                Log.d(TAG, "Ad opened: ${tapsellPlusAdModel.responseId}")
            }

            override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                isShowInProgress.set(false)
                Log.d(TAG, "Ad closed: ${tapsellPlusAdModel.responseId}")
                _adState.value = AdState.Idle
            }

            override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                Log.d(TAG, "Ad completed and user rewarded: ${tapsellPlusAdModel.responseId}")
                if (rewardDelivered.compareAndSet(false, true)) {
                    _adState.value = AdState.Rewarded(rewardCoins)
                    onRewarded(rewardCoins)
                }
            }

            override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                isShowInProgress.set(false)
                val errorMsg = tapsellPlusErrorModel.errorMessage ?: "خطا در حین نمایش ویدیو تبلیغاتی"
                Log.e(TAG, "Ad playback error: $errorMsg")
                _adState.value = AdState.Error(errorMsg)
                onError(errorMsg)
            }
        })
    }
}
