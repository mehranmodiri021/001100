package com.example.billing

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.android.vending.billing.IInAppBillingService
import com.example.billing.security.SecurityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

sealed class PurchaseResult {
    data class Success(
        val productId: String,
        val orderId: String,
        val purchaseToken: String,
        val purchaseTime: Long,
        val rawPurchaseData: String,
        val signature: String
    ) : PurchaseResult()

    data class Error(val code: Int, val message: String) : PurchaseResult()
    data class Canceled(val message: String = "خرید توسط کاربر لغو شد") : PurchaseResult()
}

data class VerifiedPurchase(
    val productId: String,
    val orderId: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    val purchaseState: Int
)

class BazaarBillingManager(private val context: Context) {

    companion object {
        private const val TAG = "BazaarBillingManager"

        // Bazaar In-App Billing Response Codes
        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8

        // Intent bundle keys
        const val RESPONSE_CODE = "RESPONSE_CODE"
        const val BUY_INTENT = "BUY_INTENT"
        const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"
        const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
        const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"
        const val INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var billingService: IInAppBillingService? = null
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val isPurchaseInProgress = AtomicBoolean(false)
    private var pendingPayload: String? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Connected to Cafe Bazaar billing service")
            billingService = IInAppBillingService.Stub.asInterface(service)
            _isServiceConnected.value = (billingService != null)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Disconnected from Cafe Bazaar billing service")
            billingService = null
            _isServiceConnected.value = false
        }
    }

    init {
        connectToBazaarService()
    }

    fun connectToBazaarService() {
        if (_isServiceConnected.value) return
        try {
            val serviceIntent = Intent(BazaarConfig.BAZAAR_BILLING_ACTION).apply {
                setPackage(BazaarConfig.BAZAAR_PACKAGE_NAME)
            }
            val bound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                Log.e(TAG, "Failed to bind to Cafe Bazaar billing service. Bazaar might not be installed.")
                _isServiceConnected.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception binding to Bazaar billing service: ${e.message}", e)
            _isServiceConnected.value = false
        }
    }

    fun disconnect() {
        try {
            if (_isServiceConnected.value) {
                context.unbindService(serviceConnection)
                _isServiceConnected.value = false
                billingService = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error unbinding service: ${e.message}")
        }
    }

    /**
     * Launches real purchase flow through Cafe Bazaar.
     * Uses ActivityResultLauncher with IntentSenderRequest to trigger real Bazaar buy intent.
     */
    fun launchPurchase(
        activityLauncher: ActivityResultLauncher<IntentSenderRequest>,
        productId: String
    ) {
        if (isPurchaseInProgress.getAndSet(true)) {
            scope.launch {
                _purchaseEvents.emit(PurchaseResult.Error(-1, "یک خرید دیگر در حال پردازش است. لطفاً شکیبا باشید."))
            }
            return
        }

        scope.launch {
            try {
                if (!BazaarConfig.isConfigured) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_DEVELOPER_ERROR,
                            "تنظیمات کلید عمومی کافه بازار ناقص است (کلید عمومی یافت نشد)."
                        )
                    )
                    return@launch
                }

                val service = billingService
                if (service == null) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE,
                            "ارتباط با سرویس پرداخت کافه بازار برقرار نشد. آیا برنامه کافه بازار نصب است؟"
                        )
                    )
                    return@launch
                }

                val developerPayload = UUID.randomUUID().toString()
                pendingPayload = developerPayload

                val buyIntentBundle = withContext(Dispatchers.IO) {
                    service.getBuyIntent(
                        BazaarConfig.BILLING_API_VERSION,
                        context.packageName,
                        productId,
                        BazaarConfig.ITEM_TYPE_INAPP,
                        developerPayload
                    )
                }

                if (buyIntentBundle == null) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "پاسخی از سرویس پرداخت کافه بازار دریافت نشد."
                        )
                    )
                    return@launch
                }

                val responseCode = buyIntentBundle.getInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_ERROR)
                if (responseCode != BILLING_RESPONSE_RESULT_OK) {
                    isPurchaseInProgress.set(false)
                    val errorMsg = mapBillingResponseCodeToMessage(responseCode)
                    _purchaseEvents.emit(PurchaseResult.Error(responseCode, errorMsg))
                    return@launch
                }

                val pendingIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    buyIntentBundle.getParcelable(BUY_INTENT, PendingIntent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    buyIntentBundle.getParcelable(BUY_INTENT)
                }
                if (pendingIntent == null) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "درخواست پرداخت معتبر از کافه بازار صادر نشد."
                        )
                    )
                    return@launch
                }

                // Launch real Bazaar buy flow via IntentSenderRequest
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                activityLauncher.launch(request)
            } catch (e: RemoteException) {
                isPurchaseInProgress.set(false)
                Log.e(TAG, "RemoteException during launchPurchase: ${e.message}", e)
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE,
                        "خطای ارتباط با سرور کافه بازار: ${e.localizedMessage}"
                    )
                )
            } catch (e: Exception) {
                isPurchaseInProgress.set(false)
                Log.e(TAG, "Exception during launchPurchase: ${e.message}", e)
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_ERROR,
                        "خطای غیرمنتظره در شروع فرآیند خرید: ${e.localizedMessage}"
                    )
                )
            }
        }
    }

    /**
     * Handles the real ActivityResult returned from Cafe Bazaar.
     * Extracts purchaseData and dataSignature, parses JSON, and performs cryptographic verification.
     */
    fun handleActivityResult(result: ActivityResult) {
        scope.launch {
            try {
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(PurchaseResult.Canceled("فرآیند خرید لغو شد."))
                    return@launch
                }

                val dataIntent = result.data
                if (dataIntent == null) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(PurchaseResult.Error(BILLING_RESPONSE_RESULT_ERROR, "داده‌های خرید دریافت نشد."))
                    return@launch
                }

                val responseCode = dataIntent.getIntExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
                if (responseCode != BILLING_RESPONSE_RESULT_OK) {
                    isPurchaseInProgress.set(false)
                    val errorMsg = mapBillingResponseCodeToMessage(responseCode)
                    _purchaseEvents.emit(PurchaseResult.Error(responseCode, errorMsg))
                    return@launch
                }

                val purchaseData = dataIntent.getStringExtra(INAPP_PURCHASE_DATA)
                val dataSignature = dataIntent.getStringExtra(INAPP_DATA_SIGNATURE)

                if (purchaseData.isNullOrBlank() || dataSignature.isNullOrBlank()) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "اطلاعات رسید خرید ناقص است (امضا یا داده موجود نیست)."
                        )
                    )
                    return@launch
                }

                // Strict Cryptographic RSA Verification
                val publicKey = BazaarConfig.bazaarPublicKey
                if (publicKey.isBlank()) {
                    isPurchaseInProgress.set(false)
                    Log.e(TAG, "FAIL CLOSED: Public key is missing. Cannot verify purchase.")
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_DEVELOPER_ERROR,
                            "اعتبارسنجی امنیتی خرید به دلیل نبود کلید عمومی ناموفق بود."
                        )
                    )
                    return@launch
                }

                val isSignatureValid = SecurityHelper.verifyPurchase(publicKey, purchaseData, dataSignature)
                if (!isSignatureValid) {
                    isPurchaseInProgress.set(false)
                    Log.e(TAG, "SECURITY ALERT: Signature verification failed for purchase data!")
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "امضای دیجیتال خرید نامعتبر است و مورد تایید قرار نگرفت."
                        )
                    )
                    return@launch
                }

                // Parse Purchase JSON
                val json = JSONObject(purchaseData)
                val productId = json.optString("productId")
                val orderId = json.optString("orderId")
                val purchaseToken = json.optString("purchaseToken")
                val purchaseTime = json.optLong("purchaseTime", System.currentTimeMillis())
                val purchaseState = json.optInt("purchaseState", -1)

                // Validation checks
                if (purchaseToken.isBlank() || purchaseToken == "verified" || purchaseToken == "restored_owned") {
                    isPurchaseInProgress.set(false)
                    Log.e(TAG, "Invalid or fake purchaseToken detected!")
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "توکن خرید نامعتبر یا جعلی است."
                        )
                    )
                    return@launch
                }

                if (purchaseState != 0) {
                    isPurchaseInProgress.set(false)
                    Log.e(TAG, "PurchaseState is not 0 (PURCHASED). State: $purchaseState")
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "وضعیت خرید نامعتبر است (خرید تکمیل نشده یا مرجوع شده)."
                        )
                    )
                    return@launch
                }

                isPurchaseInProgress.set(false)
                Log.d(TAG, "Purchase verified successfully for product: $productId, token: $purchaseToken")
                _purchaseEvents.emit(
                    PurchaseResult.Success(
                        productId = productId,
                        orderId = orderId,
                        purchaseToken = purchaseToken,
                        purchaseTime = purchaseTime,
                        rawPurchaseData = purchaseData,
                        signature = dataSignature
                    )
                )
            } catch (e: Exception) {
                isPurchaseInProgress.set(false)
                Log.e(TAG, "Exception parsing activity result: ${e.message}", e)
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_ERROR,
                        "خطا در پردازش اطلاعات خرید: ${e.localizedMessage}"
                    )
                )
            }
        }
    }

    /**
     * Restore purchases from Bazaar with mandatory cryptographic verification.
     * If Public Key is missing or invalid, returns empty list (fail-closed).
     * Only cryptographically verified purchases with purchaseState == 0 and valid tokens are returned.
     */
    suspend fun restorePurchases(): List<VerifiedPurchase> = withContext(Dispatchers.IO) {
        val verifiedList = mutableListOf<VerifiedPurchase>()
        val service = billingService
        if (service == null) {
            Log.e(TAG, "Cannot restore purchases: Bazaar service is not connected.")
            return@withContext emptyList()
        }

        val publicKey = BazaarConfig.bazaarPublicKey
        if (publicKey.isBlank()) {
            Log.e(TAG, "FAIL CLOSED: Public key is missing. No purchases can be verified during restore.")
            return@withContext emptyList()
        }

        var continuationToken: String? = null
        try {
            do {
                val bundle = service.getPurchases(
                    BazaarConfig.BILLING_API_VERSION,
                    context.packageName,
                    BazaarConfig.ITEM_TYPE_INAPP,
                    continuationToken
                ) ?: break

                val responseCode = bundle.getInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_ERROR)
                if (responseCode != BILLING_RESPONSE_RESULT_OK) {
                    Log.e(TAG, "getPurchases returned error response: $responseCode")
                    break
                }

                val purchaseDataList = bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST)
                val signatureList = bundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST)

                if (purchaseDataList != null && signatureList != null && purchaseDataList.size == signatureList.size) {
                    for (i in purchaseDataList.indices) {
                        val purchaseData = purchaseDataList[i]
                        val signature = signatureList[i]

                        // Cryptographic verification
                        if (SecurityHelper.verifyPurchase(publicKey, purchaseData, signature)) {
                            val json = JSONObject(purchaseData)
                            val productId = json.optString("productId")
                            val orderId = json.optString("orderId")
                            val purchaseToken = json.optString("purchaseToken")
                            val purchaseTime = json.optLong("purchaseTime", 0L)
                            val purchaseState = json.optInt("purchaseState", -1)

                            if (purchaseState == 0 &&
                                purchaseToken.isNotBlank() &&
                                purchaseToken != "verified" &&
                                purchaseToken != "restored_owned" &&
                                productId.isNotBlank()
                            ) {
                                verifiedList.add(
                                    VerifiedPurchase(
                                        productId = productId,
                                        orderId = orderId,
                                        purchaseToken = purchaseToken,
                                        purchaseTime = purchaseTime,
                                        purchaseState = purchaseState
                                    )
                                )
                            }
                        } else {
                            Log.w(TAG, "Unverified purchase skipped during restore")
                        }
                    }
                }

                continuationToken = bundle.getString(INAPP_CONTINUATION_TOKEN)
            } while (!continuationToken.isNullOrBlank())
        } catch (e: Exception) {
            Log.e(TAG, "Exception during restorePurchases: ${e.message}", e)
        }

        verifiedList
    }

    /**
     * Consumes a consumable item (such as coins or tickets) so it can be bought again.
     */
    suspend fun consumePurchase(purchaseToken: String): Boolean = withContext(Dispatchers.IO) {
        val service = billingService ?: return@withContext false
        try {
            val response = service.consumePurchase(
                BazaarConfig.BILLING_API_VERSION,
                context.packageName,
                purchaseToken
            )
            response == BILLING_RESPONSE_RESULT_OK
        } catch (e: Exception) {
            Log.e(TAG, "Exception consuming purchase: ${e.message}", e)
            false
        }
    }

    private fun mapBillingResponseCodeToMessage(code: Int): String {
        return when (code) {
            BILLING_RESPONSE_RESULT_USER_CANCELED -> "خرید توسط شما لغو شد."
            BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE -> "سرویس پرداخت کافه بازار در دسترس نیست."
            BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE -> "نسخه کافه بازار شما از پرداخت درون‌برنامه‌ای پشتیبانی نمی‌کند."
            BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE -> "محصول مورد نظر در کافه بازار موجود نیست یا غیرفعال شده است."
            BILLING_RESPONSE_RESULT_DEVELOPER_ERROR -> "خطای تنظیمات فنی توسعه‌دهنده در ارتباط با کافه بازار."
            BILLING_RESPONSE_RESULT_ERROR -> "خطایی در فرآیند پرداخت کافه بازار رخ داده است."
            BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED -> "شما قبلاً این محصول یا اشتراک را خریداری کرده‌اید."
            BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED -> "محصول یافت نشد یا در مالکیت شما نیست."
            else -> "خطای ناشناخته در خرید (کد: $code)"
        }
    }
}
