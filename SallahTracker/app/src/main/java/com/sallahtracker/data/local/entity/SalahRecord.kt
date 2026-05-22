package com.sallahtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.data.model.SalahType
import java.util.Date

@Entity(tableName = "salah_records")
data class SalahRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Store as timestamp for simplicity
    val type: SalahType,
    val status: SalahStatus,
    val time: String // e.g., "5:30 AM"
)