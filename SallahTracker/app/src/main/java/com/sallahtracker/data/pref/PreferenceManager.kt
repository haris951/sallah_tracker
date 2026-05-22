package com.sallahtracker.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY = stringPreferencesKey("city")
        val COUNTRY = stringPreferencesKey("country")
        val NOTIFICATION_OFFSET = intPreferencesKey("notification_offset") // in minutes
        val PRAYER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("prayer_notifications_enabled")
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
}

data class LocationPrefData(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
)