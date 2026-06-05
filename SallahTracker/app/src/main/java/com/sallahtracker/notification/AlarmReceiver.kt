package com.sallahtracker.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.sallahtracker.R
import com.sallahtracker.SallahApp
import com.sallahtracker.ui.settings.AlarmActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SNOOZE = "com.sallahtracker.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.sallahtracker.ACTION_DISMISS"
        const val ACTION_TRIGGER = "com.sallahtracker.ACTION_TRIGGER"
        const val ALARM_NOTIFICATION_ID_OFFSET = 1000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        Log.d("AlarmReceiver", "Received action: ${intent.action} for $prayerName")
        
        when (intent.action) {
            ACTION_SNOOZE -> snoozeAlarm(context, prayerName)
            ACTION_DISMISS -> {
                dismissAlarm(context, prayerName)
                Toast.makeText(context, "Alarm dismissed", Toast.LENGTH_SHORT).show()
            }
            else -> showAlarmNotification(context, prayerName)
        }
    }

    private fun showAlarmNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val app = context.applicationContext as SallahApp
        val pref = app.preferenceManager
        
        // Fetch selected sound
        val soundName = runBlocking { pref.selectedSound.first() }
        val resId = getSoundResId(soundName)
        val soundUri = Uri.parse("android.resource://${context.packageName}/$resId")
        
        // Dynamic channel ID ensures sound settings update immediately in the system
        val channelId = "prayer_alarms_ch_${soundName.hashCode()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full screen alarms for prayer times"
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("prayer_name", prayerName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, prayerName.hashCode(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra("prayer_name", prayerName)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, prayerName.hashCode() + 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("prayer_name", prayerName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, prayerName.hashCode() + 2, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time for $prayerName")
            .setContentText("May Allah accept your prayers")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Snooze", snoozePendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Dismiss", dismissPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(prayerName.hashCode() + ALARM_NOTIFICATION_ID_OFFSET, notification)
    }

    private fun snoozeAlarm(context: Context, prayerName: String) {
        dismissAlarm(context, prayerName)
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("prayer_name", prayerName)
            action = ACTION_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, prayerName.hashCode() + 10, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000)
        val info = AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
        
        Toast.makeText(context, "Alarm snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
    }

    private fun dismissAlarm(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(prayerName.hashCode() + ALARM_NOTIFICATION_ID_OFFSET)
        context.sendBroadcast(Intent("com.sallahtracker.CLOSE_ALARM"))
    }

    private fun getSoundResId(name: String): Int {
        return when (name) {
            "Allahu Akbar (Short)" -> R.raw.allahu_akbar_short
            "Adhan 1" -> R.raw.azan1
            "Islamic Nasheed" -> R.raw.islamic_nasheed
            "Quran Aayat" -> R.raw.quran_aayat
            else -> R.raw.azan1
        }
    }
}
