package com.sallahtracker.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sallahtracker.SallahApp
import com.sallahtracker.data.pref.PreferenceManager
import com.sallahtracker.notification.PrayerNotificationWorker
import com.sallahtracker.ui.base.MviViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit

class SettingsViewModel(application: Application) : AndroidViewModel(application),
    MviViewModel<SettingsState, SettingsIntent, SettingsEffect> {

    private val preferenceManager = (application as SallahApp).preferenceManager
    
    private val _uiState = MutableStateFlow(SettingsState())
    override val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsEffect>()
    override val uiEffect: SharedFlow<SettingsEffect> = _uiEffect.asSharedFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    init {
        viewModelScope.launch {
            combine(
                preferenceManager.locationData,
                preferenceManager.notificationOffset,
                preferenceManager.notificationsEnabled
            ) { location, offset, enabled ->
                setState {
                    copy(
                        detectedLocation = location?.let { 
                            LocationData(it.city, it.country, it.latitude, it.longitude) 
                        },
                        notificationOffset = offset,
                        isNotificationsEnabled = enabled
                    )
                }
            }.collect()
        }
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleDarkMode -> setState { copy(isDarkMode = intent.enabled) }
            is SettingsIntent.ToggleNotifications -> {
                viewModelScope.launch {
                    preferenceManager.setNotificationsEnabled(intent.enabled)
                    if (intent.enabled) {
                        PrayerNotificationWorker.scheduleNextPrayers(getApplication())
                    }
                }
            }
            is SettingsIntent.OpenLocationSettings -> setEffect(SettingsEffect.NavigateToLocationSettings)
            is SettingsIntent.ChangeCalculationMethod -> setEffect(SettingsEffect.ShowToast("Calculation Method Change Clicked"))
            is SettingsIntent.OpenHelpAndSupport -> setEffect(SettingsEffect.ShowToast("Help & Support Clicked"))
            is SettingsIntent.RateApp -> setEffect(SettingsEffect.ShowToast("Rate Us Clicked"))
            is SettingsIntent.DetectLocation -> detectLocation()
            is SettingsIntent.SaveLocation -> saveLocation()
            is SettingsIntent.UpdateNotificationOffset -> {
                viewModelScope.launch {
                    preferenceManager.setNotificationOffset(intent.offset)
                    PrayerNotificationWorker.scheduleNextPrayers(getApplication())
                }
            }
            is SettingsIntent.SendTestNotification -> sendTestNotification()
        }
    }

    private fun sendTestNotification() {
        val workRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(workDataOf("prayer_name" to "Test Notification"))
            .build()

        WorkManager.getInstance(getApplication()).enqueue(workRequest)
        setEffect(SettingsEffect.ShowToast("Test notification will arrive in 5 seconds"))
    }

    @SuppressLint("MissingPermission")
    private fun detectLocation() {
        viewModelScope.launch {
            setState { copy(isDetectingLocation = true) }
            try {
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val locationData = LocationData(
                            city = address.locality ?: address.subAdminArea ?: "Unknown City",
                            country = address.countryName ?: "Unknown Country",
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        setState { 
                            copy(
                                isDetectingLocation = false,
                                detectedLocation = locationData
                            ) 
                        }
                    } else {
                        setState { copy(isDetectingLocation = false) }
                        setEffect(SettingsEffect.ShowToast("Could not find address for this location"))
                    }
                } else {
                    setState { copy(isDetectingLocation = false) }
                    setEffect(SettingsEffect.ShowToast("Location not found. Enable GPS and try again."))
                }
            } catch (e: Exception) {
                setState { copy(isDetectingLocation = false) }
                setEffect(SettingsEffect.ShowToast("Error detecting location: ${e.message}"))
            }
        }
    }

    private fun saveLocation() {
        viewModelScope.launch {
            val location = uiState.value.detectedLocation
            if (location != null) {
                preferenceManager.saveLocation(
                    location.latitude,
                    location.longitude,
                    location.city,
                    location.country
                )
                setState { copy(locationSaved = true) }
                PrayerNotificationWorker.scheduleNextPrayers(getApplication())
                setEffect(SettingsEffect.ShowToast("Location saved successfully!"))
            }
        }
    }

    private fun setState(reduce: SettingsState.() -> SettingsState) {
        _uiState.value = _uiState.value.reduce()
    }

    private fun setEffect(effect: SettingsEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
