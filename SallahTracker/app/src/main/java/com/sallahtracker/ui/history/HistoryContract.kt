package com.sallahtracker.ui.history

import com.sallahtracker.ui.base.UiEffect
import com.sallahtracker.ui.base.UiIntent
import com.sallahtracker.ui.base.UiState
import java.util.Calendar

data class DaySummary(
    val date: Long = 0L,
    val completed: Int = 0,
    val missed: Int = 0,
    val total: Int = 0,
    val dateString: String = ""
) {
    val fraction: String = if (total > 0) "$completed/$total" else ""
    val percentage: Float = if (total > 0) completed.toFloat() / total else 0f
}

data class HistoryState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val daySummaryMap: Map<Long, DaySummary> = emptyMap(),
    val selectedDate: Long? = null,
    val selectedDaySummary: DaySummary? = null,
    val isLoading: Boolean = false
) : UiState

sealed class HistoryIntent : UiIntent {
    object LoadHistory : HistoryIntent()
    object NextMonth : HistoryIntent()
    object PreviousMonth : HistoryIntent()
    data class OnDaySelected(val date: Long) : HistoryIntent()
}

sealed class HistoryEffect : UiEffect {
    data class ShowToast(val message: String) : HistoryEffect()
}