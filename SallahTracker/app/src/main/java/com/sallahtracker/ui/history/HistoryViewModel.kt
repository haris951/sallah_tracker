package com.sallahtracker.ui.history

import androidx.lifecycle.viewModelScope
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.data.repository.SalahRepository
import com.sallahtracker.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(private val repository: SalahRepository) :
    BaseViewModel<HistoryState, HistoryIntent, HistoryEffect>(HistoryState()) {

    private val dayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    init {
        loadHistoryForMonth()
        selectToday()
    }

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.LoadHistory -> loadHistoryForMonth()
            is HistoryIntent.NextMonth -> changeMonth(1)
            is HistoryIntent.PreviousMonth -> changeMonth(-1)
            is HistoryIntent.OnDaySelected -> selectDay(intent.date)
        }
    }

    private fun selectToday() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        selectDay(today)
    }

    private fun selectDay(date: Long) {
        viewModelScope.launch {
            repository.getRecordsForDate(date).collectLatest { records ->
                val completed = records.count { it.status == SalahStatus.COMPLETED }
                val missed = records.count { it.status == SalahStatus.MISSED }
                val total = records.size
                
                val summary = DaySummary(
                    date = date,
                    completed = completed,
                    missed = missed,
                    total = if (total == 0 && isToday(date)) 5 else total,
                    dateString = dayFormat.format(Date(date))
                )
                
                setState { copy(selectedDate = date, selectedDaySummary = summary) }
            }
        }
    }

    private fun isToday(date: Long): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return date == today
    }

    private fun changeMonth(delta: Int) {
        val newCalendar = uiState.value.currentMonth.clone() as Calendar
        newCalendar.add(Calendar.MONTH, delta)
        setState { copy(currentMonth = newCalendar) }
        loadHistoryForMonth()
    }

    private fun loadHistoryForMonth() {
        viewModelScope.launch {
            val calendar = uiState.value.currentMonth.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            val endOfMonth = calendar.timeInMillis - 1

            repository.getRecordsInRange(startOfMonth, endOfMonth).collectLatest { records ->
                val groupedByDay = records.groupBy { it.date }
                val summaryMap = groupedByDay.mapValues { (date, dayRecords) ->
                    val completed = dayRecords.count { it.status == SalahStatus.COMPLETED }
                    val missed = dayRecords.count { it.status == SalahStatus.MISSED }
                    val total = dayRecords.size
                    DaySummary(
                        date = date,
                        completed = completed,
                        missed = missed,
                        total = total,
                        dateString = dayFormat.format(Date(date))
                    )
                }
                setState { copy(daySummaryMap = summaryMap) }
            }
        }
    }
}