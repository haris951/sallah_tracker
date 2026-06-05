package com.sallahtracker.ui.settings

import com.sallahtracker.data.model.SalahType
import com.sallahtracker.ui.base.UiEffect
import com.sallahtracker.ui.base.UiIntent
import com.sallahtracker.ui.base.UiState

data class LocationData(
    val city: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class AlarmSettings(
    val enabled: Boolean = true,
    val offset: Int = -10, // minutes
    val soundAndVibration: String = "Sound & Vibration"
)

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val notificationOffset: Int = 0,
    val calculationMethod: String = "Muslim World League",
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false,
    val isDetectingLocation: Boolean = false,
    val detectedLocation: LocationData? = null,
    val locationSaved: Boolean = false,
    
    // Alarm States
    val prayerAlarms: Map<SalahType, AlarmSettings> = SalahType.entries.associateWith { AlarmSettings() },
    val alarmVolume: Int = 75,
    val selectedAlarmSound: String = "Adhan 1",
    val availableSounds: List<String> = listOf("Adhan 1", "Allahu Akbar (Short)", "Islamic Nasheed", "Quran Aayat"),
    val isPreviewPlaying: Boolean = false
) : UiState

sealed class SettingsIntent : UiIntent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsIntent()
    data class ToggleNotifications(val enabled: Boolean) : SettingsIntent()
    data class UpdateNotificationOffset(val offset: Int) : SettingsIntent()
    object OpenLocationSettings : SettingsIntent()
    object OpenPrayerAlarms : SettingsIntent()
    object ChangeCalculationMethod : SettingsIntent()
    object OpenHelpAndSupport : SettingsIntent()
    object RateApp : SettingsIntent()
    object SendTestNotification : SettingsIntent()
    object SendTestAlarm : SettingsIntent()
    
    // Location Intents
    object DetectLocation : SettingsIntent()
    object SaveLocation : SettingsIntent()
    
    // Alarm Intents
    data class TogglePrayerAlarm(val type: SalahType, val enabled: Boolean) : SettingsIntent()
    data class UpdatePrayerAlarmOffset(val type: SalahType, val offset: Int) : SettingsIntent()
    data class SelectAlarmSound(val sound: String) : SettingsIntent()
    object StopPreviewSound : SettingsIntent()
    data class UpdateAlarmVolume(val volume: Int) : SettingsIntent()
    object SaveAlarmSettings : SettingsIntent()
}

sealed class SettingsEffect : UiEffect {
    data class ShowToast(val message: String) : SettingsEffect()
    object NavigateToLocationSettings : SettingsEffect()
    object NavigateToPrayerAlarms : SettingsEffect()
    object RequestLocationPermission : SettingsEffect()
}
