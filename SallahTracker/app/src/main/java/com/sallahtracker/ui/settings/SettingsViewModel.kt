package com.sallahtracker.ui.settings

import com.sallahtracker.ui.base.BaseViewModel

class SettingsViewModel : BaseViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState()) {

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleDarkMode -> setState { copy(isDarkMode = intent.enabled) }
            is SettingsIntent.ToggleNotifications -> setState { copy(isNotificationsEnabled = intent.enabled) }
            is SettingsIntent.ToggleAutoLocation -> setState { copy(isAutoLocationEnabled = intent.enabled) }
            is SettingsIntent.ChangeCalculationMethod -> setEffect(SettingsEffect.ShowToast("Calculation Method Change Clicked"))
            is SettingsIntent.OpenHelpAndSupport -> setEffect(SettingsEffect.ShowToast("Help & Support Clicked"))
            is SettingsIntent.RateApp -> setEffect(SettingsEffect.ShowToast("Rate Us Clicked"))
        }
    }
}