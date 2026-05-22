package com.sallahtracker.util

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.*

object SalahCalculator {

    fun getPrayerTimes(latitude: Double, longitude: Double, date: Date = Date()): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents.from(date)
        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        return PrayerTimes(coordinates, dateComponents, params)
    }

    fun formatTime(date: Date): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(date)
    }
}