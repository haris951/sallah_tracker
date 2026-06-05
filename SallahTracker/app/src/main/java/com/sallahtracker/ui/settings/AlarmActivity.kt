package com.sallahtracker.ui.settings

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallahtracker.R
import com.sallahtracker.SallahApp
import com.sallahtracker.notification.AlarmReceiver
import com.sallahtracker.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var prayerName: String = ""

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.sallahtracker.CLOSE_ALARM") {
                dismissAlarm(showToast = false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        
        // Ensure the screen stays on and shows over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Register receiver to close activity if notification is dismissed via buttons
        val filter = IntentFilter("com.sallahtracker.CLOSE_ALARM")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(closeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(closeReceiver, filter)
        }

        setupAndPlaySound()

        setContent {
            AlarmScreen(
                prayerName = prayerName,
                onStop = { dismissAlarm() },
                onSnooze = { snoozeAlarm() }
            )
        }
    }

    private fun setupAndPlaySound() {
        val app = application as SallahApp
        val pref = app.preferenceManager
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val soundName = pref.selectedSound.first()
                val volume = pref.alarmVolume.first() / 100f
                val resId = getSoundResId(soundName)

                mediaPlayer = MediaPlayer.create(this@AlarmActivity, resId).apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setVolume(volume, volume)
                    isLooping = true
                    start()
                }
            } catch (e: Exception) {
                Log.e("AlarmActivity", "Error playing alarm sound", e)
                playDefaultSound()
            }
        }
    }

    private fun playDefaultSound() {
        try {
            mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("AlarmActivity", "No sound available", e)
        }
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

    private fun snoozeAlarm() {
        stopAlarmResources()
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("prayer_name", prayerName)
            action = AlarmReceiver.ACTION_TRIGGER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            prayerName.hashCode() + 100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
        
        // Use setAlarmClock for snooze as well for highest precision
        val info = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
        
        Toast.makeText(this, "Alarm snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun dismissAlarm(showToast: Boolean = true) {
        stopAlarmResources()
        if (showToast) {
            Toast.makeText(this, "Alarm dismissed", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun stopAlarmResources() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Error stopping player", e)
        }
        mediaPlayer = null
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(prayerName.hashCode())
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(closeReceiver)
        } catch (e: Exception) {
            // Ignored
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun AlarmScreen(prayerName: String, onStop: () -> Unit, onSnooze: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PrimaryGreen
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "It's time for",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 20.sp
            )
            
            Text(
                text = prayerName,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "May Allah accept your prayers",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("STOP ALARM", color = PrimaryGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                border = BorderStroke(1.dp, Color.White),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SNOOZE (10 MINS)", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
