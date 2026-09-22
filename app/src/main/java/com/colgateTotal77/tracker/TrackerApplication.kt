package com.colgateTotal77.tracker

import android.app.Application
import androidx.room.Room
import com.colgateTotal77.tracker.core.UserPreferencesRepository
import com.colgateTotal77.tracker.core.dataStore
import com.colgateTotal77.tracker.core.database.AppDatabase

class TrackerApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "tracker_database"
        )
            .addCallback(AppDatabase.CALLBACK)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()
    }

    val userPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}