package com.sallahtracker.ui.settings

import com.sallahtracker.ui.base.UiEffect
import com.sallahtracker.ui.base.UiIntent
import com.sallahtracker.ui.base.UiState

data class LocationData(
    val city: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val notificationOffset: Int = 0, // Minutes before (-) or after (+)
    val calculationMethod: String = "Muslim World League",
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false,
    val isDetectingLocation: Boolean = false,
    val detectedLocation: LocationData? = null,
    val locationSaved: Boolean = false
) : UiState

sealed class SettingsIntent : UiIntent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsIntent()
    data class ToggleNotifications(val enabled: Boolean) : SettingsIntent()
    data class UpdateNotificationOffset(val offset: Int) : SettingsIntent()
    object OpenLocationSettings : SettingsIntent()
    object ChangeCalculationMethod : SettingsIntent()
    object OpenHelpAndSupport : SettingsIntent()
    object RateApp : SettingsIntent()
    object SendTestNotification : SettingsIntent()
    
    // Location Intents
    object DetectLocation : SettingsIntent()
    object SaveLocation : SettingsIntent()
}

sealed class SettingsEffect : UiEffect {
    data class ShowToast(val message: String) : SettingsEffect()
    object NavigateToLocationSettings : SettingsEffect()
    object RequestLocationPermission : SettingsEffect()
}