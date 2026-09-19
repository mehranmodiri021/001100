package com.android.vending.billing

import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel

/**
 * Standard Android In-App Billing IPC interface used by Cafe Bazaar.
 * Implemented directly via android.os.IInterface and Binder proxy for maximum compatibility
 * without requiring custom AIDL plugin tooling.
 */
interface IInAppBillingService : IInterface {

    fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int

    fun getSkuDetails(apiVersion: Int, packageName: String, type: String, skusBundle: Bundle): Bundle?

    fun getBuyIntent(
        apiVersion: Int,
        packageName: String,
        sku: String,
        type: String,
        developerPayload: String
    ): Bundle?

    fun getPurchases(
        apiVersion: Int,
        packageName: String,
        type: String,
        continuationToken: String?
    ): Bundle?

    fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int

    abstract class Stub : android.os.Binder(), IInAppBillingService {
        companion object {
            private const val DESCRIPTOR = "com.android.vending.billing.IInAppBillingService"
            const val TRANSACTION_isBillingSupported = IBinder.FIRST_CALL_TRANSACTION + 0
            const val TRANSACTION_getSkuDetails = IBinder.FIRST_CALL_TRANSACTION + 1
            const val TRANSACTION_getBuyIntent = IBinder.FIRST_CALL_TRANSACTION + 2
            const val TRANSACTION_getPurchases = IBinder.FIRST_CALL_TRANSACTION + 3
            const val TRANSACTION_consumePurchase = IBinder.FIRST_CALL_TRANSACTION + 4

            fun asInterface(binder: IBinder?): IInAppBillingService? {
                if (binder == null) return null
                val iin = binder.queryLocalInterface(DESCRIPTOR)
                if (iin is IInAppBillingService) return iin
                return Proxy(binder)
            }
        }

        private class Proxy(private val remote: IBinder) : IInAppBillingService {
            override fun asBinder(): IBinder = remote

            override fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(apiVersion)
                    data.writeString(packageName)
                    data.writeString(type)
                    remote.transact(TRANSACTION_isBillingSupported, data, reply, 0)
                    reply.readException()
                    reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getSkuDetails(
                apiVersion: Int,
                packageName: String,
                type: String,
                skusBundle: Bundle
            ): Bundle? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(apiVersion)
                    data.writeString(packageName)
                    data.writeString(type)
                    data.writeInt(1)
                    skusBundle.writeToParcel(data, 0)
                    remote.transact(TRANSACTION_getSkuDetails, data, reply, 0)
                    reply.readException()
                    if (reply.readInt() != 0) Bundle.CREATOR.createFromParcel(reply) else null
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getBuyIntent(
                apiVersion: Int,
                packageName: String,
                sku: String,
                type: String,
                developerPayload: String
            ): Bundle? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(apiVersion)
                    data.writeString(packageName)
                    data.writeString(sku)
                    data.writeString(type)
                    data.writeString(developerPayload)
                    remote.transact(TRANSACTION_getBuyIntent, data, reply, 0)
                    reply.readException()
                    if (reply.readInt() != 0) Bundle.CREATOR.createFromParcel(reply) else null
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun getPurchases(
                apiVersion: Int,
                packageName: String,
                type: String,
                continuationToken: String?
            ): Bundle? {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(apiVersion)
                    data.writeString(packageName)
                    data.writeString(type)
                    data.writeString(continuationToken)
                    remote.transact(TRANSACTION_getPurchases, data, reply, 0)
                    reply.readException()
                    if (reply.readInt() != 0) Bundle.CREATOR.createFromParcel(reply) else null
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }

            override fun consumePurchase(
                apiVersion: Int,
                packageName: String,
                purchaseToken: String
            ): Int {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                return try {
                    data.writeInterfaceToken(DESCRIPTOR)
                    data.writeInt(apiVersion)
                    data.writeString(packageName)
                    data.writeString(purchaseToken)
                    remote.transact(TRANSACTION_consumePurchase, data, reply, 0)
                    reply.readException()
                    reply.readInt()
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            }
        }
    }
}
