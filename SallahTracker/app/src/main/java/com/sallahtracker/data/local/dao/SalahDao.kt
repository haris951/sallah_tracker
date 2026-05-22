package com.sallahtracker.data.local.dao

import androidx.room.*
import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.data.model.SalahType
import kotlinx.coroutines.flow.Flow

@Dao
interface SalahDao {
    @Query("SELECT * FROM salah_records WHERE date = :date")
    fun getRecordsForDate(date: Long): Flow<List<SalahRecord>>

    @Query("SELECT * FROM salah_records WHERE status = 'MISSED'")
    fun getAllMissedRecords(): Flow<List<SalahRecord>>

    @Query("SELECT * FROM salah_records WHERE date >= :startDate AND date <= :endDate")
    fun getRecordsInRange(startDate: Long, endDate: Long): Flow<List<SalahRecord>>

    @Query("SELECT * FROM salah_records")
    fun getAllRecords(): Flow<List<SalahRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SalahRecord)

    @Update
    suspend fun updateRecord(record: SalahRecord)

    @Query("DELETE FROM salah_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
}