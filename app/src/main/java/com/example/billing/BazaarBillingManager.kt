package com.arenaclash.game.billing

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
import com.arenaclash.game.billing.security.SecurityHelper
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
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

sealed class BillingConnectionState {
    data object Disconnected : BillingConnectionState()
    data object Connecting : BillingConnectionState()
    data object Connected : BillingConnectionState()
    data class Error(val message: String) : BillingConnectionState()
}

sealed class PurchaseResult {
    data class Success(val purchase: VerifiedPurchase) : PurchaseResult()
    data class Error(val responseCode: Int, val message: String) : PurchaseResult()
    data class VerificationFailed(val reason: String) : PurchaseResult()
    data object UserCancelled : PurchaseResult()
}

data class VerifiedPurchase(
    val orderId: String,
    val packageName: String,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: Int,
    val developerPayload: String?,
    val purchaseToken: String,
    val originalJson: String,
    val signature: String
)

class BazaarBillingManager(private val context: Context) {

    companion object {
        private const val TAG = "BazaarBillingManager"
        private const val BAZAAR_PACKAGE = "com.farsitel.bazaar"
        private const val BAZAAR_BILLING_ACTION = "ir.cafebazaar.pardakht.InAppBillingService.BIND"
        private const val BILLING_API_VERSION = 3
        private const val ITEM_TYPE_INAPP = "inapp"

        // Response keys
        private const val RESPONSE_CODE = "RESPONSE_CODE"
        private const val BUY_INTENT = "BUY_INTENT"
        private const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        private const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"
        private const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
        private const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        private const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"
        private const val INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN"

        // Response codes
        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var billingService: IBinder? = null
    private val isPurchaseInProgress = AtomicBoolean(false)

    private val _connectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(replay = 0)
    val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Connected to Cafe Bazaar In-App Billing service.")
            billingService = service
            _connectionState.value = BillingConnectionState.Connected
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Disconnected from Cafe Bazaar In-App Billing service.")
            billingService = null
            _connectionState.value = BillingConnectionState.Disconnected
        }
    }

    fun connectToBazaarService() {
        if (_connectionState.value == BillingConnectionState.Connected) {
            return
        }

        _connectionState.value = BillingConnectionState.Connecting
        val serviceIntent = Intent(BAZAAR_BILLING_ACTION).apply {
            setPackage(BAZAAR_PACKAGE)
        }

        try {
            val bound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                Log.e(TAG, "Could not bind to Cafe Bazaar Billing service. Is Cafe Bazaar installed?")
                _connectionState.value = BillingConnectionState.Error("کافه بازار بر روی دستگاه نصب نیست یا اتصال برقرار نشد.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding to Bazaar billing service: ${e.message}", e)
            _connectionState.value = BillingConnectionState.Error("خطا در اتصال به سرویس بازار: ${e.message}")
        }
    }

    fun launchPurchaseFlow(
        activityLauncher: ActivityResultLauncher<IntentSenderRequest>,
        productId: String,
        developerPayload: String = ""
    ) {
        if (!isPurchaseInProgress.compareAndSet(false, true)) {
            Log.w(TAG, "Purchase flow already in progress. Ignoring duplicate call.")
            return
        }

        val service = billingService
        if (service == null) {
            isPurchaseInProgress.set(false)
            coroutineScope.launch {
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE,
                        "سرویس کافه بازار در دسترس نیست. لطفا مطمئن شوید بازار فعال است."
                    )
                )
            }
            return
        }

        coroutineScope.launch {
            try {
                val data = android.os.Parcel.obtain()
                val reply = android.os.Parcel.obtain()
                val buyIntentBundle: Bundle?
                try {
                    data.writeInterfaceToken("com.android.vending.billing.IInAppBillingService")
                    data.writeInt(BILLING_API_VERSION)
                    data.writeString(context.packageName)
                    data.writeString(productId)
                    data.writeString(ITEM_TYPE_INAPP)
                    data.writeString(developerPayload)

                    // TRANSACTION_getBuyIntent = IBinder.FIRST_CALL_TRANSACTION + 2
                    service.transact(IBinder.FIRST_CALL_TRANSACTION + 2, data, reply, 0)
                    reply.readException()
                    buyIntentBundle = if (reply.readInt() != 0) {
                        Bundle.CREATOR.createFromParcel(reply)
                    } else {
                        null
                    }
                } finally {
                    data.recycle()
                    reply.recycle()
                }

                if (buyIntentBundle == null) {
                    isPurchaseInProgress.set(false)
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "پاسخی از سرور کافه بازار دریافت نشد."
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

                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                activityLauncher.launch(request)
            } catch (e: RemoteException) {
                isPurchaseInProgress.set(false)
                Log.e(TAG, "RemoteException during launchPurchase: ${e.message}", e)
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_ERROR,
                        "خطای ارتباط با سرور پرداخت بازار: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                isPurchaseInProgress.set(false)
                Log.e(TAG, "Exception launching purchase flow: ${e.message}", e)
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_ERROR,
                        "خطای غیرمنتظره در شروع خرید: ${e.message}"
                    )
                )
            }
        }
    }

    fun handleActivityResult(result: ActivityResult) {
        isPurchaseInProgress.set(false)
        if (result.resultCode == Activity.RESULT_CANCELED) {
            coroutineScope.launch {
                _purchaseEvents.emit(PurchaseResult.UserCancelled)
            }
            return
        }

        val dataIntent = result.data
        if (result.resultCode != Activity.RESULT_OK || dataIntent == null) {
            coroutineScope.launch {
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_ERROR,
                        "عملیات خرید تکمیل نشد."
                    )
                )
            }
            return
        }

        val responseCode = dataIntent.getIntExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
        val purchaseData = dataIntent.getStringExtra(INAPP_PURCHASE_DATA)
        val dataSignature = dataIntent.getStringExtra(INAPP_DATA_SIGNATURE)

        if (responseCode != BILLING_RESPONSE_RESULT_OK || purchaseData.isNullOrBlank() || dataSignature.isNullOrBlank()) {
            val errorMsg = mapBillingResponseCodeToMessage(responseCode)
            coroutineScope.launch {
                _purchaseEvents.emit(PurchaseResult.Error(responseCode, errorMsg))
            }
            return
        }

        verifyAndEmitPurchase(purchaseData, dataSignature)
    }

    private fun verifyAndEmitPurchase(purchaseData: String, dataSignature: String) {
        coroutineScope.launch {
            val publicKey = BazaarConfig.bazaarPublicKey
            if (publicKey.isBlank()) {
                Log.e(TAG, "Fail-Closed: Bazaar RSA Public Key is missing in BuildConfig.")
                _purchaseEvents.emit(
                    PurchaseResult.VerificationFailed(
                        "کلید عمومی بازار تنظیم نشده است. به دلایل امنیتی خرید تایید نمی‌شود."
                    )
                )
                return@launch
            }

            val isValid = SecurityHelper.verifyPurchase(publicKey, purchaseData, dataSignature)
            if (!isValid) {
                Log.e(TAG, "Security verification failed for purchaseData: $purchaseData")
                _purchaseEvents.emit(
                    PurchaseResult.VerificationFailed(
                        "امضای دیجیتال خرید نامعتبر است و مورد تایید قرار نگرفت."
                    )
                )
                return@launch
            }

            try {
                val json = JSONObject(purchaseData)
                val orderId = json.optString("orderId")
                val packageName = json.optString("packageName")
                val productId = json.getString("productId")
                val purchaseTime = json.getLong("purchaseTime")
                val purchaseState = json.getInt("purchaseState")
                val developerPayload = if (json.has("developerPayload") && !json.isNull("developerPayload")) json.getString("developerPayload") else null
                val purchaseToken = json.getString("purchaseToken")

                if (purchaseState != 0) {
                    _purchaseEvents.emit(
                        PurchaseResult.Error(
                            BILLING_RESPONSE_RESULT_ERROR,
                            "وضعیت خرید نامعتبر است (وضعیت: $purchaseState)"
                        )
                    )
                    return@launch
                }

                val verifiedPurchase = VerifiedPurchase(
                    orderId = orderId,
                    packageName = packageName,
                    productId = productId,
                    purchaseTime = purchaseTime,
                    purchaseState = purchaseState,
                    developerPayload = developerPayload,
                    purchaseToken = purchaseToken,
                    originalJson = purchaseData,
                    signature = dataSignature
                )

                _purchaseEvents.emit(PurchaseResult.Success(verifiedPurchase))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing verified purchase JSON: ${e.message}", e)
                _purchaseEvents.emit(
                    PurchaseResult.Error(
                        BILLING_RESPONSE_RESULT_ERROR,
                        "خطا در پردازش اطلاعات خرید: ${e.message}"
                    )
                )
            }
        }
    }

    fun queryPurchases(onResult: (List<VerifiedPurchase>) -> Unit) {
        val service = billingService
        if (service == null) {
            onResult(emptyList())
            return
        }

        coroutineScope.launch {
            val verifiedList = mutableListOf<VerifiedPurchase>()
            var continuationToken: String? = null
            val publicKey = BazaarConfig.bazaarPublicKey

            if (publicKey.isBlank()) {
                Log.w(TAG, "Cannot query and verify purchases: Public key not configured.")
                onResult(emptyList())
                return@launch
            }

            try {
                do {
                    val data = android.os.Parcel.obtain()
                    val reply = android.os.Parcel.obtain()
                    val ownedItemsBundle: Bundle?
                    try {
                        data.writeInterfaceToken("com.android.vending.billing.IInAppBillingService")
                        data.writeInt(BILLING_API_VERSION)
                        data.writeString(context.packageName)
                        data.writeString(ITEM_TYPE_INAPP)
                        data.writeString(continuationToken)

                        // TRANSACTION_getPurchases = IBinder.FIRST_CALL_TRANSACTION + 3
                        service.transact(IBinder.FIRST_CALL_TRANSACTION + 3, data, reply, 0)
                        reply.readException()
                        ownedItemsBundle = if (reply.readInt() != 0) {
                            Bundle.CREATOR.createFromParcel(reply)
                        } else {
                            null
                        }
                    } finally {
                        data.recycle()
                        reply.recycle()
                    }

                    if (ownedItemsBundle == null) break

                    val response = ownedItemsBundle.getInt(RESPONSE_CODE)
                    if (response != BILLING_RESPONSE_RESULT_OK) break

                    val purchaseDataList = ownedItemsBundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST)
                    val signatureList = ownedItemsBundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST)

                    if (purchaseDataList != null && signatureList != null) {
                        for (i in 0 until purchaseDataList.size) {
                            val purchaseJson = purchaseDataList[i]
                            val signature = signatureList.getOrNull(i).orEmpty()

                            if (SecurityHelper.verifyPurchase(publicKey, purchaseJson, signature)) {
                                val json = JSONObject(purchaseJson)
                                val purchaseState = json.getInt("purchaseState")
                                if (purchaseState == 0) {
                                    verifiedList.add(
                                        VerifiedPurchase(
                                            orderId = json.optString("orderId"),
                                            packageName = json.optString("packageName"),
                                            productId = json.getString("productId"),
                                            purchaseTime = json.getLong("purchaseTime"),
                                            purchaseState = purchaseState,
                                            developerPayload = if (json.has("developerPayload") && !json.isNull("developerPayload")) json.getString("developerPayload") else null,
                                            purchaseToken = json.getString("purchaseToken"),
                                            originalJson = purchaseJson,
                                            signature = signature
                                        )
                                    )
                                }
                            }
                        }
                    }

                    continuationToken = ownedItemsBundle.getString(INAPP_CONTINUATION_TOKEN)
                } while (!continuationToken.isNullOrBlank())

                onResult(verifiedList)
            } catch (e: Exception) {
                Log.e(TAG, "Error querying purchases: ${e.message}", e)
                onResult(emptyList())
            }
        }
    }

    fun consumePurchase(purchaseToken: String, onConsumed: (Boolean) -> Unit = {}) {
        val service = billingService
        if (service == null) {
            onConsumed(false)
            return
        }

        coroutineScope.launch {
            try {
                val data = android.os.Parcel.obtain()
                val reply = android.os.Parcel.obtain()
                val responseCode: Int
                try {
                    data.writeInterfaceToken("com.android.vending.billing.IInAppBillingService")
                    data.writeInt(BILLING_API_VERSION)
                    data.writeString(context.packageName)
                    data.writeString(purchaseToken)

                    // TRANSACTION_consumePurchase = IBinder.FIRST_CALL_TRANSACTION + 4
                    service.transact(IBinder.FIRST_CALL_TRANSACTION + 4, data, reply, 0)
                    reply.readException()
                    responseCode = reply.readInt()
                } finally {
                    data.recycle()
                    reply.recycle()
                }

                val success = (responseCode == BILLING_RESPONSE_RESULT_OK)
                onConsumed(success)
            } catch (e: Exception) {
                Log.e(TAG, "Error consuming purchase: ${e.message}", e)
                onConsumed(false)
            }
        }
    }

    fun disconnect() {
        if (_connectionState.value == BillingConnectionState.Connected) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.w(TAG, "Error unbinding billing service: ${e.message}")
            } finally {
                billingService = null
                _connectionState.value = BillingConnectionState.Disconnected
            }
        }
    }

    private fun mapBillingResponseCodeToMessage(code: Int): String {
        return when (code) {
            BILLING_RESPONSE_RESULT_USER_CANCELED -> "پرداخت توسط کاربر لغو گردید."
            BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE -> "سرویس پرداخت کافه بازار در دسترس نیست."
            BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE -> "محصول مورد نظر در کافه بازار موجود نمی‌باشد."
            BILLING_RESPONSE_RESULT_DEVELOPER_ERROR -> "خطای توسعه‌دهنده در درخواست به بازار رخ داد."
            BILLING_RESPONSE_RESULT_ERROR -> "خطای ناشناخته در انجام عملیات پرداخت کافه بازار."
            BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED -> "شما قبلاً این محصول را خریداری کرده‌اید."
            BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED -> "این محصول در مالکیت شما نمی‌باشد."
            else -> "پاسخ نامشخص از کافه بازار (کد: $code)"
        }
    }
}
