package com.sallahtracker.ui.settings

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallahtracker.ui.theme.*

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        
        // Play default notification sound or specific alarm sound
        // In a real app, you'd load the sound selected in preferences
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        setContent {
            AlarmScreen(
                prayerName = prayerName,
                onDismiss = {
                    mediaPlayer?.stop()
                    finish()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun AlarmScreen(prayerName: String, onDismiss: () -> Unit) {
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
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(
                    text = "Dismiss Alarm",
                    color = PrimaryGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}