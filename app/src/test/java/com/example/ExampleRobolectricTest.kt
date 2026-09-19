package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.billing.BazaarConfig
import com.example.billing.security.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("آرنا کلش", appName)
    }

    @Test
    fun `securityHelper fails closed on missing parameters`() {
        // Must fail closed (return false) on missing public key
        assertFalse(SecurityHelper.verifyPurchase("", "data", "sig"))
        assertFalse(SecurityHelper.verifyPurchase(null, "data", "sig"))

        // Must fail closed on missing signed data
        assertFalse(SecurityHelper.verifyPurchase("public_key", "", "sig"))
        assertFalse(SecurityHelper.verifyPurchase("public_key", null, "sig"))

        // Must fail closed on missing signature
        assertFalse(SecurityHelper.verifyPurchase("public_key", "data", ""))
        assertFalse(SecurityHelper.verifyPurchase("public_key", "data", null))

        // Must fail closed on invalid keys
        assertFalse(SecurityHelper.verifyPurchase("invalid_base64", "data", "sig"))
    }

    @Test
    fun `bazaarConfig fail closed when unconfigured`() {
        // Without a real key from Secrets Panel, isConfigured must be false
        assertEquals("", BazaarConfig.bazaarPublicKey)
        assertFalse(BazaarConfig.isConfigured)
    }
}
