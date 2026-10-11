package com.colgateTotal77.tracker

import android.app.Application
import androidx.room.Room
import com.colgateTotal77.tracker.core.dataStore
import com.colgateTotal77.tracker.core.database.AppDatabase
import com.colgateTotal77.tracker.screens.overview.OverviewPreferences
import com.colgateTotal77.tracker.screens.settings.SettingsPreferences

class TrackerApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "tracker_database"
        )
            .addCallback(AppDatabase.CALLBACK)
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_5_6,
            )
            .build()
    }

    val overviewPreferencesRepository by lazy {
        OverviewPreferences(dataStore)
    }

    val settingsPreferencesRepository by lazy {
        SettingsPreferences(dataStore)
    }
}