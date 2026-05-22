package com.sallahtracker.ui.settings

import com.sallahtracker.ui.base.UiEffect
import com.sallahtracker.ui.base.UiIntent
import com.sallahtracker.ui.base.UiState

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val isAutoLocationEnabled: Boolean = true,
    val calculationMethod: String = "Muslim World League",
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
) : UiState

sealed class SettingsIntent : UiIntent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsIntent()
    data class ToggleNotifications(val enabled: Boolean) : SettingsIntent()
    data class ToggleAutoLocation(val enabled: Boolean) : SettingsIntent()
    object ChangeCalculationMethod : SettingsIntent()
    object OpenHelpAndSupport : SettingsIntent()
    object RateApp : SettingsIntent()
}

sealed class SettingsEffect : UiEffect {
    data class ShowToast(val message: String) : SettingsEffect()
}