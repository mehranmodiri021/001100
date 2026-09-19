package com.example.billing

import com.example.BuildConfig

object BazaarConfig {
    // Cafe Bazaar Service constants
    const val BAZAAR_PACKAGE_NAME = "com.farsitel.bazaar"
    const val BAZAAR_BILLING_ACTION = "ir.cafebazaar.pardakht.InAppBillingService.BIND"
    const val BILLING_API_VERSION = 3
    const val ITEM_TYPE_INAPP = "inapp"

    // Product IDs
    const val PRODUCT_VIP_MONTHLY = "challenge_arena_vip_monthly"
    const val PRODUCT_VIP_YEARLY = "challenge_arena_vip_yearly"
    const val PRODUCT_COINS_1000 = "challenge_arena_coins_1000"
    const val PRODUCT_COINS_5000 = "challenge_arena_coins_5000"
    const val PRODUCT_TICKETS_10 = "challenge_arena_tickets_10"

    val ALL_PRODUCTS = listOf(
        PRODUCT_VIP_MONTHLY,
        PRODUCT_VIP_YEARLY,
        PRODUCT_COINS_1000,
        PRODUCT_COINS_5000,
        PRODUCT_TICKETS_10
    )

    /**
     * Retrieve Bazaar RSA Public Key from BuildConfig / Secrets panel.
     * If not configured or empty, verification MUST fail-closed (never return true).
     */
    val bazaarPublicKey: String
        get() = try {
            val field = BuildConfig::class.java.getField("BAZAAR_PUBLIC_KEY")
            val key = (field.get(null) as? String)?.trim().orEmpty()
            if (key == "DEFAULT_BAZAAR_PUBLIC_KEY" || key == "YOUR_BAZAAR_PUBLIC_KEY") "" else key
        } catch (_: Exception) {
            ""
        }

    val isConfigured: Boolean
        get() = bazaarPublicKey.isNotBlank()
}
