package com.example.billing

import com.example.BuildConfig

object BazaarConfig {
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
