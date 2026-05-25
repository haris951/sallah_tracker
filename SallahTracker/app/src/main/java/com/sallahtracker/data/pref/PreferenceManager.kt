package com.sallahtracker.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sallahtracker.data.model.SalahType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY = stringPreferencesKey("city")
        val COUNTRY = stringPreferencesKey("country")
        val NOTIFICATION_OFFSET = intPreferencesKey("notification_offset")
        val PRAYER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("prayer_notifications_enabled")
        
        // Alarm Specific
        val ALARM_VOLUME = intPreferencesKey("alarm_volume")
        val SELECTED_SOUND = stringPreferencesKey("selected_alarm_sound")
        
        fun prayerAlarmKey(type: SalahType) = booleanPreferencesKey("alarm_enabled_${type.name}")
        fun prayerOffsetKey(type: SalahType) = intPreferencesKey("alarm_offset_${type.name}")
    }

    val locationData: Flow<LocationPrefData?> = context.dataStore.data.map { preferences ->
        val lat = preferences[LATITUDE]
        val lon = preferences[LONGITUDE]
        if (lat != null && lon != null) {
            LocationPrefData(
                lat, lon, 
                preferences[CITY] ?: "", 
                preferences[COUNTRY] ?: ""
            )
        } else null
    }

    val notificationOffset: Flow<Int> = context.dataStore.data.map { it[NOTIFICATION_OFFSET] ?: 0 }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[PRAYER_NOTIFICATIONS_ENABLED] ?: true }
    val alarmVolume: Flow<Int> = context.dataStore.data.map { it[ALARM_VOLUME] ?: 75 }
    val selectedSound: Flow<String> = context.dataStore.data.map { it[SELECTED_SOUND] ?: "Adhan" }

    fun isAlarmEnabled(type: SalahType): Flow<Boolean> = context.dataStore.data.map { it[prayerAlarmKey(type)] ?: true }
    fun getPrayerOffset(type: SalahType): Flow<Int> = context.dataStore.data.map { it[prayerOffsetKey(type)] ?: -10 }

    suspend fun saveLocation(lat: Double, lon: Double, city: String, country: String) {
        context.dataStore.edit { preferences ->
            preferences[LATITUDE] = lat
            preferences[LONGITUDE] = lon
            preferences[CITY] = city
            preferences[COUNTRY] = country
        }
    }

    suspend fun setNotificationOffset(minutes: Int) {
        context.dataStore.edit { it[NOTIFICATION_OFFSET] = minutes }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PRAYER_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setAlarmEnabled(type: SalahType, enabled: Boolean) {
        context.dataStore.edit { it[prayerAlarmKey(type)] = enabled }
    }

    suspend fun setPrayerOffset(type: SalahType, minutes: Int) {
        context.dataStore.edit { it[prayerOffsetKey(type)] = minutes }
    }

    suspend fun setAlarmVolume(volume: Int) {
        context.dataStore.edit { it[ALARM_VOLUME] = volume }
    }

    suspend fun setSelectedSound(sound: String) {
        context.dataStore.edit { it[SELECTED_SOUND] = sound }
    }
}

data class LocationPrefData(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
)