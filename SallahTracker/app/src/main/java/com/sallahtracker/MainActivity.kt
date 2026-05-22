package com.sallahtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sallahtracker.notification.PrayerNotificationWorker
import com.sallahtracker.ui.MainScreen
import com.sallahtracker.ui.theme.SallahTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            schedulePrayers()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkNotificationPermission()

        setContent {
            SallahTrackerTheme {
                MainScreen()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    schedulePrayers()
                }
                else -> requestPermissionLauncher.launch(permission)
            }
        } else {
            schedulePrayers()
        }
    }

    private fun schedulePrayers() {
        lifecycleScope.launch {
            PrayerNotificationWorker.scheduleNextPrayers(applicationContext)
        }
    }
}