package com.sallahtracker.data.repository

import com.sallahtracker.data.local.dao.SalahDao
import com.sallahtracker.data.local.entity.SalahRecord
import kotlinx.coroutines.flow.Flow

class SalahRepository(private val salahDao: SalahDao) {
    fun getRecordsForDate(date: Long): Flow<List<SalahRecord>> = salahDao.getRecordsForDate(date)
    
    fun getAllMissedRecords(): Flow<List<SalahRecord>> = salahDao.getAllMissedRecords()

    fun getRecordsInRange(startDate: Long, endDate: Long): Flow<List<SalahRecord>> = 
        salahDao.getRecordsInRange(startDate, endDate)
    
    fun getAllRecords(): Flow<List<SalahRecord>> = salahDao.getAllRecords()
    
    suspend fun updateRecord(record: SalahRecord) = salahDao.updateRecord(record)
    
    suspend fun insertRecord(record: SalahRecord) = salahDao.insertRecord(record)

    suspend fun deleteRecord(id: Long) = salahDao.deleteRecord(id)
}