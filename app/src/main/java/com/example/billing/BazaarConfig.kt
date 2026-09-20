package com.arenaclash.game.billing

import com.arenaclash.game.BuildConfig

object BazaarConfig {
    // ─── شناسه محصولات (SKU) در کافه‌بازار ───
    // ⚠️ مهم: این شناسه‌ها باید دقیقاً با آنچه در پنل کافه‌بازار تعریف شده یکی باشند
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

    // کلید عمومی RSA کافه‌بازار از BuildConfig خوانده می‌شود
    // (تزریق شده از .env توسط secrets plugin)
    val bazaarPublicKey: String
        get() = try {
            val key = BuildConfig.BAZAAR_PUBLIC_KEY.trim()
            if (key == "DEFAULT_BAZAAR_PUBLIC_KEY" || key == "YOUR_BAZAAR_PUBLIC_KEY") "" else key
        } catch (_: Exception) {
            ""
        }

    val isConfigured: Boolean
        get() = bazaarPublicKey.isNotBlank()
}
