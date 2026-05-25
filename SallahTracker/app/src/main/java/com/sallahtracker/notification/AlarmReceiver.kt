package com.sallahtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sallahtracker.ui.settings.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        Log.d("AlarmReceiver", "Alarm received for $prayerName")

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("prayer_name", prayerName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        
        context.startActivity(alarmIntent)
    }
}