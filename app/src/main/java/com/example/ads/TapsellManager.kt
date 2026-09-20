package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.example.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
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
        const val REWARDED_ZONE_ID = "6aabb9d48c70901d2f6656ed"
        const val BANNER_ZONE_ID = "6aabb9e98c70901d2f6656ee"
        const val INTERSTITIAL_ZONE_ID = "6aac02bbdd01711161ca6efa"

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

    fun requestAndShowRewardedVideo(
        activity: Activity,
        zoneId: String = REWARDED_ZONE_ID,
        rewardCoins: Int = 150,
        onRewarded: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentState = _adState.value
        if (currentState is AdState.Ready) {
            showRewardedVideo(
                activity = activity,
                responseId = currentState.responseId,
                rewardCoins = rewardCoins,
                onRewarded = onRewarded,
                onError = onError
            )
            return
        }

        if (!isInitialized) {
            val appKey = tapsellAppKey
            if (appKey.isBlank()) {
                val err = "کلید تپسل در فایل تنظیمات برنامه پیکربندی نشده است."
                _adState.value = AdState.Error(err)
                onError(err)
                return
            }
            initialize(activity) { success ->
                if (success) {
                    requestAndShowRewardedVideo(activity, zoneId, rewardCoins, onRewarded, onError)
                } else {
                    val err = "خطا در اتصال و راه‌اندازی سرویس تبلیغات تپسل."
                    _adState.value = AdState.Error(err)
                    onError(err)
                }
            }
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
                Log.d(TAG, "Rewarded ad ready: $responseId. Showing ad now.")
                _adState.value = AdState.Ready(responseId)
                showRewardedVideo(
                    activity = activity,
                    responseId = responseId,
                    rewardCoins = rewardCoins,
                    onRewarded = onRewarded,
                    onError = onError
                )
            }

            override fun error(errorMessage: String?) {
                isRequestInProgress.set(false)
                val msg = errorMessage ?: "خطا در دریافت ویدیوی جایزه‌دار تپسل"
                Log.e(TAG, "Error requesting rewarded ad: $msg")
                _adState.value = AdState.Error(msg)
                onError(msg)
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

    fun requestAndShowInterstitial(
        activity: Activity,
        zoneId: String = INTERSTITIAL_ZONE_ID,
        onClosed: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isInitialized) {
            val appKey = tapsellAppKey
            if (appKey.isBlank()) {
                onError?.invoke("کلید تپسل در فایل تنظیمات برنامه پیکربندی نشده است.")
                return
            }
            initialize(activity) { success ->
                if (success) {
                    requestAndShowInterstitial(activity, zoneId, onClosed, onError)
                } else {
                    onError?.invoke("مقداردهی اولیه سرویس تپسل ناموفق بود.")
                }
            }
            return
        }

        TapsellPlus.requestInterstitialAd(
            activity,
            zoneId,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    val responseId = tapsellPlusAdModel.responseId
                    TapsellPlus.showInterstitialAd(
                        activity,
                        responseId,
                        object : AdShowListener() {
                            override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                                Log.d(TAG, "Interstitial ad opened: ${tapsellPlusAdModel.responseId}")
                            }

                            override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                                Log.d(TAG, "Interstitial ad closed: ${tapsellPlusAdModel.responseId}")
                                onClosed?.invoke()
                            }

                            override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                                val err = tapsellPlusErrorModel.errorMessage ?: "خطا در نمایش ویدیوی فوری"
                                Log.e(TAG, "Interstitial show error: $err")
                                onError?.invoke(err)
                            }
                        }
                    )
                }

                override fun error(errorMessage: String?) {
                    val err = errorMessage ?: "خطا در دریافت ویدیوی فوری تپسل"
                    Log.e(TAG, "Interstitial request error: $err")
                    onError?.invoke(err)
                }
            }
        )
    }

    fun requestAndShowBanner(
        activity: Activity,
        container: ViewGroup,
        zoneId: String = BANNER_ZONE_ID,
        onShown: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isInitialized) {
            val appKey = tapsellAppKey
            if (appKey.isBlank()) {
                onError?.invoke("کلید تپسل در فایل تنظیمات برنامه پیکربندی نشده است.")
                return
            }
            initialize(activity) { success ->
                if (success) {
                    requestAndShowBanner(activity, container, zoneId, onShown, onError)
                } else {
                    onError?.invoke("مقداردهی اولیه سرویس تپسل ناموفق بود.")
                }
            }
            return
        }

        TapsellPlus.requestStandardBannerAd(
            activity,
            zoneId,
            TapsellPlusBannerType.BANNER_320x50,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    val responseId = tapsellPlusAdModel.responseId
                    TapsellPlus.showStandardBannerAd(
                        activity,
                        responseId,
                        container,
                        object : AdShowListener() {
                            override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                                Log.d(TAG, "Standard Banner opened: $responseId")
                                onShown?.invoke()
                            }

                            override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                                val err = tapsellPlusErrorModel.errorMessage ?: "خطا در نمایش بنر تبلیغاتی"
                                Log.e(TAG, "Standard Banner show error: $err")
                                onError?.invoke(err)
                            }
                        }
                    )
                }

                override fun error(errorMessage: String?) {
                    val err = errorMessage ?: "خطا در دریافت بنر تبلیغاتی تپسل"
                    Log.e(TAG, "Standard Banner request error: $err")
                    onError?.invoke(err)
                }
            }
        )
    }

    fun destroyBanner(activity: Activity, container: ViewGroup, responseId: String = "") {
        try {
            TapsellPlus.destroyStandardBanner(activity, responseId, container)
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying standard banner: ${e.message}")
        }
    }
}
