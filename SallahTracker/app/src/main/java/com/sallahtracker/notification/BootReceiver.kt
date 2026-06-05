package com.sallahtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = PrayerAlarmScheduler(context)
            CoroutineScope(Dispatchers.IO).launch {
                scheduler.scheduleAlarms()
            }
        }
    }
}
