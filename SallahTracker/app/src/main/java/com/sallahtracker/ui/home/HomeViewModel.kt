package com.sallahtracker.ui.home

import androidx.lifecycle.viewModelScope
import com.sallahtracker.SallahApp
import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.data.model.SalahType
import com.sallahtracker.data.repository.SalahRepository
import com.sallahtracker.notification.PrayerAlarmScheduler
import com.sallahtracker.notification.PrayerNotificationWorker
import com.sallahtracker.ui.base.BaseViewModel
import com.sallahtracker.util.SalahCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: SalahRepository,
    private val app: SallahApp
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    private val preferenceManager = app.preferenceManager

    init {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        setState { copy(date = sdf.format(Date())) }
        
        viewModelScope.launch {
            val today = getTodayTimestamp()
            
            combine(
                repository.getRecordsForDate(today),
                preferenceManager.locationData
            ) { records, location ->
                if (records.isEmpty()) {
                    initializeDefaultPrayers(today, location)
                } else {
                    val updatedRecords = if (location != null) {
                        val prayerTimes = SalahCalculator.getPrayerTimes(location.latitude, location.longitude)
                        records.map { record ->
                            val newTime = when (record.type) {
                                SalahType.FAJR -> SalahCalculator.formatTime(prayerTimes.fajr)
                                SalahType.ZUHR -> SalahCalculator.formatTime(prayerTimes.dhuhr)
                                SalahType.ASR -> record.time
                                SalahType.MAGHRIB -> SalahCalculator.formatTime(prayerTimes.maghrib)
                                SalahType.ISHA -> SalahCalculator.formatTime(prayerTimes.isha)
                            }
                            record.copy(time = newTime)
                        }
                    } else records

                    val completed = updatedRecords.count { it.status == SalahStatus.COMPLETED }
                    setState { 
                        copy(
                            prayers = updatedRecords,
                            completedCount = completed,
                            totalCount = updatedRecords.size
                        ) 
                    }
                    
                    // Alarms scheduled after records are confirmed to exist
                    launch {
                        PrayerNotificationWorker.scheduleNextPrayers(app)
                        PrayerAlarmScheduler(app).scheduleAlarms()
                    }
                }
            }.collect()
        }
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadTodayPrayers -> { }
            is HomeIntent.UpdateSalahStatus -> updateStatus(intent.record, intent.newStatus)
            is HomeIntent.MarkAllCompleted -> markAllCompleted()
            is HomeIntent.SaveDay -> saveDay()
        }
    }

    private suspend fun initializeDefaultPrayers(date: Long, location: com.sallahtracker.data.pref.LocationPrefData?) {
        val prayerTimes = if (location != null) {
            SalahCalculator.getPrayerTimes(location.latitude, location.longitude)
        } else null

        val defaults = listOf(
            SalahRecord(date = date, type = SalahType.FAJR, status = SalahStatus.PENDING, 
                time = prayerTimes?.let { SalahCalculator.formatTime(it.fajr) } ?: "4:30 AM"),
            SalahRecord(date = date, type = SalahType.ZUHR, status = SalahStatus.PENDING, 
                time = prayerTimes?.let { SalahCalculator.formatTime(it.dhuhr) } ?: "1:30 PM"),
            SalahRecord(date = date, type = SalahType.ASR, status = SalahStatus.PENDING, 
                time = "5:30 PM"),
            SalahRecord(date = date, type = SalahType.MAGHRIB, status = SalahStatus.PENDING, 
                time = prayerTimes?.let { SalahCalculator.formatTime(it.maghrib) } ?: "7:10 PM"),
            SalahRecord(date = date, type = SalahType.ISHA, status = SalahStatus.PENDING, 
                time = prayerTimes?.let { SalahCalculator.formatTime(it.isha) } ?: "8:45 PM")
        )
        defaults.forEach { repository.insertRecord(it) }
        
        // Alarms scheduled after initial insertion
        PrayerNotificationWorker.scheduleNextPrayers(app)
        PrayerAlarmScheduler(app).scheduleAlarms()
    }

    private fun updateStatus(record: SalahRecord, newStatus: SalahStatus) {
        viewModelScope.launch {
            repository.updateRecord(record.copy(status = newStatus))
        }
    }

    private fun markAllCompleted() {
        viewModelScope.launch {
            uiState.value.prayers.forEach {
                repository.updateRecord(it.copy(status = SalahStatus.COMPLETED))
            }
        }
    }

    private fun saveDay() {
        setEffect(HomeEffect.ShowToast("Day saved successfully!"))
    }

    private fun getTodayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
