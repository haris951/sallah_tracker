package com.sallahtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sallahtracker.data.local.dao.SalahDao
import com.sallahtracker.data.local.entity.SalahRecord

@Database(entities = [SalahRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salahDao(): SalahDao
}