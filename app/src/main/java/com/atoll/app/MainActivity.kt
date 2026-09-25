package com.atoll.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.atoll.app.services.AdsService
import com.atoll.app.services.BillingService
import com.atoll.app.services.Referrer
import com.atoll.app.ui.AppState
import com.atoll.app.ui.AtollRoot
import com.atoll.app.ui.AtollTheme

class MainActivity : ComponentActivity() {
    private lateinit var state: AppState

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AtollApp
        val ads = AdsService(applicationContext)
        lateinit var billing: BillingService
        billing = BillingService(applicationContext) { owned -> runOnUiThread { state.markAdsRemoved(owned) } }
        state = AppState(this, lifecycleScope, ads, billing)

        setContent { AtollTheme { AtollRoot(state) } }

        ads.gatherConsent(this)
        billing.connect()
        state.onStart()
        state.handleIntent(intent)
        Referrer.read(this, app.store) { ref -> runOnUiThread { state.handleReferrer(ref) } }
        if (!app.store.notificationAsked && app.store.dailiesPlayed >= 1) askNotificationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        state.handleIntent(intent)
    }

    override fun onStop() {
        state.saveCurrent()
        super.onStop()
    }

    fun askNotificationPermission() {
        val app = application as AtollApp
        app.store.notificationAsked = true
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
