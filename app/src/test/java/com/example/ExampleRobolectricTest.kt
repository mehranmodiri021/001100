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
  fun `security helper fails closed when public key is blank`() {
    val result = SecurityHelper.verifyPurchase(
      base64PublicKey = "",
      signedData = "{\"orderId\":\"123\"}",
      signature = "sig"
    )
    assertFalse(result)
  }

  @Test
  fun `bazaar config filters default placeholder key`() {
    // When key is default placeholder, isConfigured should be false
    assertFalse(BazaarConfig.isConfigured)
  }

  @Test
  fun `navigation bottom items are initialized and non null`() {
    val items = com.example.ui.navigation.Screen.bottomNavItems
    assertEquals(6, items.size)
    items.forEach { screen ->
      org.junit.Assert.assertNotNull(screen)
      org.junit.Assert.assertTrue(screen.route.isNotBlank())
    }
  }
}
