package com.sallahtracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.sallahtracker.MainActivity
import com.sallahtracker.SallahApp
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PrayerNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prayerName = inputData.getString("prayer_name") ?: return Result.failure()
        
        Log.d("PrayerWorker", "Triggering notification for: $prayerName")
        showNotification(prayerName)
        
        // Reschedule for next prayers
        scheduleNextPrayers(applicationContext)
        
        return Result.success()
    }

    private fun showNotification(prayerName: String) {
        val channelId = "prayer_reminders"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Time for $prayerName")
            .setContentText("It's time for $prayerName prayer. May Allah accept your prayers.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }

    companion object {
        suspend fun scheduleNextPrayers(context: Context) {
            val app = context.applicationContext as SallahApp
            val repository = app.repository
            val pref = app.preferenceManager
            
            val enabled = pref.notificationsEnabled.first()
            if (!enabled) return

            // Get today's timestamp (midnight)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Fetch records from Database (This makes it respect your hardcoded 4:16 PM)
            val records = repository.getRecordsForDate(today).first()
            val offset = pref.notificationOffset.first()
            val now = Date()
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())

            val workManager = WorkManager.getInstance(context)

            records.forEach { record ->
                try {
                    val prayerDate = sdf.parse(record.time) ?: return@forEach
                    val calendar = Calendar.getInstance()
                    val timeCal = Calendar.getInstance().apply { time = prayerDate }
                    
                    calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, 0)
                    calendar.add(Calendar.MINUTE, offset)

                    val delay = calendar.timeInMillis - now.time
                    if (delay > 0) {
                        Log.d("PrayerWorker", "Scheduled ${record.type.displayName} at ${calendar.time} (In ${delay/1000}s)")
                        val workRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(workDataOf("prayer_name" to record.type.displayName))
                            .addTag("prayer_${record.type.name}")
                            .build()

                        workManager.enqueueUniqueWork(
                            "prayer_${record.type.name}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest
                        )
                    }
                } catch (e: Exception) {
                    Log.e("PrayerWorker", "Error parsing time for ${record.type}: ${record.time}")
                }
            }
        }
    }
}