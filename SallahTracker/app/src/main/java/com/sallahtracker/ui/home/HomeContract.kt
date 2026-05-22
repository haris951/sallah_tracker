package com.sallahtracker.ui.home

import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.ui.base.UiEffect
import com.sallahtracker.ui.base.UiIntent
import com.sallahtracker.ui.base.UiState

data class HomeState(
    val date: String = "",
    val prayers: List<SalahRecord> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 5,
    val isLoading: Boolean = false
) : UiState

sealed class HomeIntent : UiIntent {
    object LoadTodayPrayers : HomeIntent()
    data class UpdateSalahStatus(val record: SalahRecord, val newStatus: SalahStatus) : HomeIntent()
    object MarkAllCompleted : HomeIntent()
    object SaveDay : HomeIntent()
}

sealed class HomeEffect : UiEffect {
    data class ShowToast(val message: String) : HomeEffect()
}