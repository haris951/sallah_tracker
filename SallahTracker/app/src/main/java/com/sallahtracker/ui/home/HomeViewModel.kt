package com.sallahtracker.ui.home

import androidx.lifecycle.viewModelScope
import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.data.model.SalahType
import com.sallahtracker.data.repository.SalahRepository
import com.sallahtracker.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val repository: SalahRepository) :
    BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        setState { copy(date = sdf.format(Date())) }
        handleIntent(HomeIntent.LoadTodayPrayers)
    }

    override fun onIntent(intent: HomeIntent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadTodayPrayers -> loadPrayers()
            is HomeIntent.UpdateSalahStatus -> updateStatus(intent.record, intent.newStatus)
            is HomeIntent.MarkAllCompleted -> markAllCompleted()
            is HomeIntent.SaveDay -> saveDay()
        }
    }

    private fun loadPrayers() {
        viewModelScope.launch {
            val today = getTodayTimestamp()
            repository.getRecordsForDate(today).collectLatest { records ->
                if (records.isEmpty()) {
                    initializeDefaultPrayers(today)
                } else {
                    val completed = records.count { it.status == SalahStatus.COMPLETED }
                    setState { 
                        copy(
                            prayers = records,
                            completedCount = completed,
                            totalCount = records.size
                        ) 
                    }
                }
            }
        }
    }

    private suspend fun initializeDefaultPrayers(date: Long) {
        val defaults = listOf(
            SalahRecord(date = date, type = SalahType.FAJR, status = SalahStatus.PENDING, time = "5:30 AM"),
            SalahRecord(date = date, type = SalahType.ZUHR, status = SalahStatus.PENDING, time = "1:15 PM"),
            SalahRecord(date = date, type = SalahType.ASR, status = SalahStatus.PENDING, time = "4:45 PM"),
            SalahRecord(date = date, type = SalahType.MAGHRIB, status = SalahStatus.PENDING, time = "7:20 PM"),
            SalahRecord(date = date, type = SalahType.ISHA, status = SalahStatus.PENDING, time = "8:45 PM")
        )
        defaults.forEach { repository.insertRecord(it) }
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