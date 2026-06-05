package com.sallahtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.sallahtracker.data.local.AppDatabase
import com.sallahtracker.data.repository.SalahRepository
import com.sallahtracker.data.pref.PreferenceManager

class SallahApp : Application() {
    
    lateinit var repository: SalahRepository
        private set

    lateinit var preferenceManager: PreferenceManager
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sallah_db"
        ).build()
        
        repository = SalahRepository(database.salahDao())
        preferenceManager = PreferenceManager(applicationContext)
        
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel 1: Standard Prayer Notifications
            val reminderChannel = NotificationChannel(
                "prayer_reminders",
                "Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Standard notifications for prayer times"
            }
            notificationManager.createNotificationChannel(reminderChannel)
            
            // Channel 2: Full-screen Prayer Alarms
            val alarmChannel = NotificationChannel(
                "prayer_alarms_channel",
                "Prayer Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full screen alarms for prayer times"
                setSound(null, null) // Sound is handled by AlarmActivity
                enableVibration(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }
}