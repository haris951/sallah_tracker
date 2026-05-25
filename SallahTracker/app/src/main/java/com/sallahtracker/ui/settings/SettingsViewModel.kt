package com.sallahtracker.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sallahtracker.R
import com.sallahtracker.SallahApp
import com.sallahtracker.data.model.SalahType
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
    private var mediaPlayer: MediaPlayer? = null

    init {
        // Collect state from preferences
        viewModelScope.launch {
            val flows = listOf(
                preferenceManager.locationData,
                preferenceManager.notificationsEnabled,
                preferenceManager.notificationOffset,
                preferenceManager.alarmVolume,
                preferenceManager.selectedSound
            )
            
            combine(flows) { array ->
                val location = array[0] as? com.sallahtracker.data.pref.LocationPrefData
                val notificationsEnabled = array[1] as Boolean
                val offset = array[2] as Int
                val volume = array[3] as Int
                val sound = array[4] as String
                
                // Update live volume if playing
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            val vol = volume / 100f
                            player.setVolume(vol, vol)
                        }
                    } catch (e: Exception) {
                        Log.e("SettingsViewModel", "Error setting player volume", e)
                    }
                }

                _uiState.value.copy(
                    detectedLocation = location?.let { 
                        LocationData(it.city, it.country, it.latitude, it.longitude) 
                    },
                    isNotificationsEnabled = notificationsEnabled,
                    notificationOffset = offset,
                    alarmVolume = volume,
                    selectedAlarmSound = sound,
                    locationSaved = location != null
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }

        // Load individual prayer alarms
        SalahType.entries.forEach { type ->
            viewModelScope.launch {
                combine(
                    preferenceManager.isAlarmEnabled(type),
                    preferenceManager.getPrayerOffset(type)
                ) { enabled, offset ->
                    setState {
                        val currentAlarms = prayerAlarms.toMutableMap()
                        currentAlarms[type] = AlarmSettings(enabled = enabled, offset = offset)
                        copy(prayerAlarms = currentAlarms)
                    }
                }.collect()
            }
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
            is SettingsIntent.UpdateNotificationOffset -> {
                viewModelScope.launch {
                    preferenceManager.setNotificationOffset(intent.offset)
                    PrayerNotificationWorker.scheduleNextPrayers(getApplication())
                }
            }
            is SettingsIntent.OpenLocationSettings -> setEffect(SettingsEffect.NavigateToLocationSettings)
            is SettingsIntent.OpenPrayerAlarms -> setEffect(SettingsEffect.NavigateToPrayerAlarms)
            is SettingsIntent.ChangeCalculationMethod -> setEffect(SettingsEffect.ShowToast("Calculation Method Change Clicked"))
            is SettingsIntent.OpenHelpAndSupport -> setEffect(SettingsEffect.ShowToast("Help & Support Clicked"))
            is SettingsIntent.RateApp -> setEffect(SettingsEffect.ShowToast("Rate Us Clicked"))
            is SettingsIntent.SendTestNotification -> sendTestNotification()
            is SettingsIntent.DetectLocation -> detectLocation()
            is SettingsIntent.SaveLocation -> saveLocation()
            
            // Alarm Intents
            is SettingsIntent.TogglePrayerAlarm -> {
                viewModelScope.launch {
                    preferenceManager.setAlarmEnabled(intent.type, intent.enabled)
                }
            }
            is SettingsIntent.UpdatePrayerAlarmOffset -> {
                viewModelScope.launch {
                    preferenceManager.setPrayerOffset(intent.type, intent.offset)
                }
            }
            is SettingsIntent.SelectAlarmSound -> {
                viewModelScope.launch {
                    preferenceManager.setSelectedSound(intent.sound)
                    playPreviewSound(intent.sound)
                }
            }
            is SettingsIntent.StopPreviewSound -> stopPreviewSound()
            is SettingsIntent.UpdateAlarmVolume -> {
                viewModelScope.launch {
                    preferenceManager.setAlarmVolume(intent.volume)
                }
            }
            is SettingsIntent.SaveAlarmSettings -> {
                viewModelScope.launch {
                    PrayerNotificationWorker.scheduleNextPrayers(getApplication())
                    setEffect(SettingsEffect.ShowToast("Alarm settings saved!"))
                }
            }
        }
    }

    private fun playPreviewSound(soundName: String) {
        try {
            stopPreviewSound()

            val resId = getSoundResId(soundName)
            if (resId == 0) return

            mediaPlayer = MediaPlayer.create(getApplication(), resId).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                val vol = uiState.value.alarmVolume / 100f
                setVolume(vol, vol)
                setOnCompletionListener {
                    setState { copy(isPreviewPlaying = false) }
                }
                start()
            }
            setState { copy(isPreviewPlaying = true) }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error playing preview sound", e)
            setState { copy(isPreviewPlaying = false) }
        }
    }

    private fun stopPreviewSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            setState { copy(isPreviewPlaying = false) }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error stopping sound", e)
        }
    }

    private fun getSoundResId(name: String): Int {
        return when (name) {
            "Allahu Akbar (Short)" -> R.raw.allahu_akbar_short
            "Adhan 1" -> R.raw.azan1
            "Islamic Nasheed" -> R.raw.islamic_nasheed
            "Quran Aayat" -> R.raw.quran_aayat
            else -> 0
        }
    }

    private fun sendTestNotification() {
        val workRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(workDataOf("prayer_name" to "Test Alarm"))
            .build()

        WorkManager.getInstance(getApplication()).enqueue(workRequest)
        setEffect(SettingsEffect.ShowToast("Test alarm will arrive in 5 seconds"))
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

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
