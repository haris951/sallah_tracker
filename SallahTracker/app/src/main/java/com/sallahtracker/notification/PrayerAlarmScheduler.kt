package com.sallahtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sallahtracker.SallahApp
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleAlarms() {
        val app = context.applicationContext as SallahApp
        val repository = app.repository
        val pref = app.preferenceManager

        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val records = repository.getRecordsForDate(todayMidnight).first()
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())

        if (records.isEmpty()) {
            Log.d("AlarmScheduler", "No records found in database to schedule alarms.")
            return
        }

        records.forEach { record ->
            val isEnabled = pref.isAlarmEnabled(record.type).first()
            if (!isEnabled) {
                cancelAlarm(record.type.name.hashCode())
                return@forEach
            }

            try {
                val prayerDate = sdf.parse(record.time) ?: return@forEach
                val timeCal = Calendar.getInstance().apply { time = prayerDate }
                
                // Set the exact time for today
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // Apply the prayer-specific offset
                val offset = pref.getPrayerOffset(record.type).first()
                calendar.add(Calendar.MINUTE, offset)

                // If the alarm time has already passed today, schedule it for tomorrow
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                scheduleExactAlarm(
                    calendar.timeInMillis,
                    record.type.displayName,
                    record.type.name.hashCode()
                )
                
                Log.d("AlarmScheduler", "Scheduled alarm for ${record.type.displayName} at ${calendar.time} (ID: ${record.type.name.hashCode()})")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Error scheduling alarm for ${record.type}", e)
            }
        }
    }

    private fun scheduleExactAlarm(timeInMillis: Long, prayerName: String, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("prayer_name", prayerName)
            action = AlarmReceiver.ACTION_TRIGGER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Using setAlarmClock for the best user experience (system alarm icon + highest precision)
        val info = AlarmManager.AlarmClockInfo(timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
