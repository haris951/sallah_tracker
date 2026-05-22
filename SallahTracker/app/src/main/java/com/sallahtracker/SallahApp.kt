package com.sallahtracker

import android.app.Application
import androidx.room.Room
import com.sallahtracker.data.local.AppDatabase
import com.sallahtracker.data.repository.SalahRepository

class SallahApp : Application() {
    
    lateinit var repository: SalahRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sallah_db"
        ).build()
        
        repository = SalahRepository(database.salahDao())
    }
}