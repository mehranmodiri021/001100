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
        private const val BAZAAR_BILLING_ACTION =
            "ir.cafebazaar.pardakht.InAppBillingService.BIND"

        private const val BILLING_API_VERSION = 3
        private const val ITEM_TYPE_INAPP = "inapp"

        private const val RESPONSE_CODE = "RESPONSE_CODE"
        private const val BUY_INTENT = "BUY_INTENT"
        private const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        private const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"
        private const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        private const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"
        private const val INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN"

        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8
    }

    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var billingService: IBinder? = null

    private val isPurchaseInProgress = AtomicBoolean(false)

    private val _connectionState =
        MutableStateFlow<BillingConnectionState>(
            BillingConnectionState.Disconnected
        )

    val connectionState: StateFlow<BillingConnectionState> =
        _connectionState.asStateFlow()

    private val _purchaseEvents =
        MutableSharedFlow<PurchaseResult>(replay = 0)

    val purchaseEvents: SharedFlow<PurchaseResult> =
        _purchaseEvents.asSharedFlow()

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            if (service == null) {
                Log.e(TAG, "Cafe Bazaar billing service returned a null binder.")
                billingService = null
                _connectionState.value =
                    BillingConnectionState.Error(
                        "سرویس پرداخت بازار معتبر نیست."
                    )
                return
            }

            Log.d(
                TAG,
                "Connected to Cafe Bazaar In-App Billing service."
            )

            billingService = service
            _connectionState.value =
                BillingConnectionState.Connected
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(
                TAG,
                "Disconnected from Cafe Bazaar In-App Billing service."
            )

            billingService = null
            _connectionState.value =
                BillingConnectionState.Disconnected
        }
    }

    fun connectToBazaarService() {
        if (_connectionState.value == BillingConnectionState.Connected) {
            return
        }

        _connectionState.value =
            BillingConnectionState.Connecting

        val serviceIntent = Intent(BAZAAR_BILLING_ACTION).apply {
            setPackage(BAZAAR_PACKAGE)
        }

       
