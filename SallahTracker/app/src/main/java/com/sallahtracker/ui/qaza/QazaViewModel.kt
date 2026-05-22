package com.sallahtracker.ui.qaza

import androidx.lifecycle.viewModelScope
import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.data.repository.SalahRepository
import com.sallahtracker.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class QazaViewModel(private val repository: SalahRepository) :
    BaseViewModel<QazaState, QazaIntent, QazaEffect>(QazaState()) {

    init {
        handleIntent(QazaIntent.LoadMissedPrayers)
    }

    override fun onIntent(intent: QazaIntent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: QazaIntent) {
        when (intent) {
            is QazaIntent.LoadMissedPrayers -> loadMissedPrayers()
            is QazaIntent.MarkAsDone -> markAsDone(intent.record)
            is QazaIntent.DeleteQaza -> deleteQaza(intent.id)
            is QazaIntent.EditQaza -> editQaza(intent.record)
            is QazaIntent.AddQazaManual -> addQazaManual()
        }
    }

    private fun loadMissedPrayers() {
        viewModelScope.launch {
            repository.getAllMissedRecords().collectLatest { records ->
                val grouped = records.groupBy { it.date }
                setState {
                    copy(
                        missedPrayersByDate = grouped,
                        totalMissedCount = records.size,
                        completedMissedCount = 0 // In this specific UI, "Missed" means they haven't been made up yet
                    )
                }
            }
        }
    }

    private fun markAsDone(record: SalahRecord) {
        viewModelScope.launch {
            // Marking a Qaza as done usually means changing status to COMPLETED
            // It will then disappear from this screen because the DAO query filters by 'MISSED'
            repository.updateRecord(record.copy(status = SalahStatus.COMPLETED))
            setEffect(QazaEffect.ShowToast("${record.type.displayName} marked as completed"))
        }
    }

    private fun deleteQaza(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            setEffect(QazaEffect.ShowToast("Record deleted"))
        }
    }

    private fun editQaza(record: SalahRecord) {
        // Implementation for edit dialog/screen
        setEffect(QazaEffect.ShowToast("Edit ${record.type.displayName}"))
    }

    private fun addQazaManual() {
        // Implementation for add dialog/screen
        setEffect(QazaEffect.ShowToast("Add manual Qaza"))
    }
}