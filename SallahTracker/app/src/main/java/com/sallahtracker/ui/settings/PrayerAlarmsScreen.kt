package com.sallahtracker.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallahtracker.data.model.SalahType
import com.sallahtracker.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerAlarmsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SettingsEffect.NavigateToLocationSettings -> { /* Handled in main settings */ }
                is SettingsEffect.NavigateToPrayerAlarms -> { /* Already here */ }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prayer Alarms",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Configure prayer reminders",
                            fontSize = 14.sp,
                            color = TextLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onIntent(SettingsIntent.SendTestAlarm) }) {
                        Text("Test Alarm", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeBackground)
            )
        },
        containerColor = BeigeBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                Text(
                    text = "PRAYER ALARMS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Prayer Alarm Cards
            items(SalahType.entries) { type ->
                val settings = state.prayerAlarms[type] ?: AlarmSettings()
                AlarmItemCard(
                    type = type,
                    settings = settings,
                    selectedSound = state.selectedAlarmSound,
                    onToggle = { viewModel.onIntent(SettingsIntent.TogglePrayerAlarm(type, it)) },
                    onOffsetChange = { viewModel.onIntent(SettingsIntent.UpdatePrayerAlarmOffset(type, it)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALARM SOUND",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Alarm Sound List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        state.availableSounds.forEachIndexed { index, name ->
                            val isSelected = state.selectedAlarmSound == name
                            SoundItemRow(
                                name = name,
                                isSelected = isSelected,
                                isPlaying = isSelected && state.isPreviewPlaying,
                                onSelect = { viewModel.onIntent(SettingsIntent.SelectAlarmSound(name)) },
                                onStop = { viewModel.onIntent(SettingsIntent.StopPreviewSound) }
                            )
                            if (index < state.availableSounds.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALARM VOLUME",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Volume Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TextLight)
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                value = state.alarmVolume.toFloat(),
                                onValueChange = { viewModel.onIntent(SettingsIntent.UpdateAlarmVolume(it.toInt())) },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryGreen,
                                    activeTrackColor = PrimaryGreen,
                                    inactiveTrackColor = PendingGrey
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "${state.alarmVolume}%", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Text(
                            text = "Adjust the volume level for prayer alarms",
                            fontSize = 12.sp,
                            color = TextLight,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Smart Features Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2ECE1))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Smart Alarm Features", fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(
                            "High priority full-screen alerts",
                            "Exact timing with AlarmManager",
                            "Persists even after phone restart"
                        ).forEach { feature ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(text = "•", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = feature, fontSize = 14.sp, color = TextLight)
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.onIntent(SettingsIntent.SaveAlarmSettings) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Save Settings", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    type: SalahType,
    settings: AlarmSettings,
    selectedSound: String,
    onToggle: (Boolean) -> Unit,
    onOffsetChange: (Int) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = type.displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    val offsetText = when {
                        settings.offset < 0 -> "${Math.abs(settings.offset)} min before"
                        settings.offset > 0 -> "${settings.offset} min after"
                        else -> "On time"
                    }
                    Text(text = "$offsetText • $selectedSound", fontSize = 14.sp, color = TextLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showOptions) "Hide Options" else "Show Options",
                        fontSize = 14.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showOptions = !showOptions }
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = onToggle,
                    thumbContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = PendingGrey,
                        uncheckedBorderColor = Color.Transparent,
                        checkedIconColor = PrimaryGreen,
                        uncheckedIconColor = Color.Gray
                    )
                )
            }

            AnimatedVisibility(visible = showOptions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(BeigeBackground, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Remind me:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onOffsetChange(settings.offset - 5) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Earlier")
                        }
                        
                        Text(
                            text = when {
                                settings.offset < 0 -> "${Math.abs(settings.offset)} mins before"
                                settings.offset > 0 -> "${settings.offset} mins after"
                                else -> "On time"
                            },
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen
                        )

                        IconButton(onClick = { onOffsetChange(settings.offset + 5) }) {
                            Icon(Icons.Default.Add, contentDescription = "Later")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundItemRow(
    name: String,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) PrimaryGreen else BeigeBackground)
                .border(1.dp, if (isSelected) PrimaryGreen else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
        IconButton(onClick = { if (isPlaying) onStop() else onSelect() }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Stop" else "Play",
                tint = if (isPlaying) Color.Red else if (isSelected) PrimaryGreen else TextLight
            )
        }
    }
}
