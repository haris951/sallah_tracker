package com.sallahtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sallahtracker.ui.MainScreen
import com.sallahtracker.ui.theme.SallahTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SallahTrackerTheme {
                MainScreen()
            }
        }
    }
}