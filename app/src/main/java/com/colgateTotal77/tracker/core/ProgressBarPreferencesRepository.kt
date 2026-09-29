package com.colgateTotal77.tracker.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.colgateTotal77.tracker.core.enums.DateFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ProgressBarPreferences(
    var targetBudget: Double = 1000.0,
    val selectedFilter: DateFilter = DateFilter.Month
)

class ProgressBarPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val KEY = stringPreferencesKey("settings")

    val progressBarSettingsFlow: Flow<ProgressBarPreferences> = dataStore.data.map { prefs ->
        val savedString = prefs[KEY] ?: return@map ProgressBarPreferences()

        val parts = savedString.split("|")

        val budget = parts.getOrNull(0)?.toDoubleOrNull() ?: 1000.0

        val filterName = parts.getOrNull(1) ?: DateFilter.Month.name
        val filter = runCatching { DateFilter.valueOf(filterName) }
            .getOrDefault(DateFilter.Month)

        ProgressBarPreferences(targetBudget = budget, selectedFilter = filter)
    }

    suspend fun updateProgressBarPreferences(settings: ProgressBarPreferences) {
        dataStore.edit { prefs ->
            val stringToSave = "${settings.targetBudget}|${settings.selectedFilter.name}"
            prefs[KEY] = stringToSave
        }
    }
}

fun calculateTargetBudget (selectedFilter: DateFilter, rawTargetBudget: Double): Double {
    return when(selectedFilter) {
        DateFilter.AllTime -> -1.0
        DateFilter.Month,
        DateFilter.PrevMonth -> rawTargetBudget
        DateFilter.SixMonth,
        DateFilter.PrevSixMonth -> rawTargetBudget * 6
        DateFilter.Year,
        DateFilter.PrevYear -> rawTargetBudget * 12
    }
}

