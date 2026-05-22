package com.sallahtracker.ui.qaza

import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.ui.base.UiEffect
import com.sallahtracker.ui.base.UiIntent
import com.sallahtracker.ui.base.UiState

data class QazaState(
    val missedPrayersByDate: Map<Long, List<SalahRecord>> = emptyMap(),
    val totalMissedCount: Int = 0,
    val completedMissedCount: Int = 0,
    val isLoading: Boolean = false
) : UiState

sealed class QazaIntent : UiIntent {
    object LoadMissedPrayers : QazaIntent()
    data class MarkAsDone(val record: SalahRecord) : QazaIntent()
    data class DeleteQaza(val id: Long) : QazaIntent()
    data class EditQaza(val record: SalahRecord) : QazaIntent()
    object AddQazaManual : QazaIntent()
}

sealed class QazaEffect : UiEffect {
    data class ShowToast(val message: String) : QazaEffect()
}