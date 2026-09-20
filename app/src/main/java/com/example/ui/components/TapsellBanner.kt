package com.arenaclash.game.ui.components

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.arenaclash.game.ads.TapsellManager

@Composable
fun TapsellBanner(
    modifier: Modifier = Modifier,
    zoneId: String = TapsellManager.BANNER_ZONE_ID
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    val container = remember(context) {
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    DisposableEffect(activity, zoneId) {
        // TapsellManager یک object است، پس بدون getInstance() صداش می‌زنیم
        TapsellManager.requestAndShowBanner(
            activity = activity,
            container = container,
            zoneId = zoneId
        )

        onDispose {
            TapsellManager.destroyBanner(
                activity = activity,
                container = container
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { container },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
