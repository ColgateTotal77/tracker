package com.colgateTotal77.tracker.screens.overview

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

data class ExchangeRatesPreferences(
    val usdUahExchangeRate: Double = 0.0,
    val eurUahExchangeRate: Double = 0.0,
    val updatedAt: Long = 0
)

class OverviewPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    private val PROGRESS_BAR_SETTINGS_KEY = stringPreferencesKey("progressBarSettings")
    private val EXCHANGE_RATES_KEY = stringPreferencesKey("exchangeRates")

    val progressBarSettingsFlow: Flow<ProgressBarPreferences> = dataStore.data.map { prefs ->
        val savedString = prefs[PROGRESS_BAR_SETTINGS_KEY] ?: return@map ProgressBarPreferences()

        val parts = savedString.split("|")

        val budget = parts.getOrNull(0)?.toDoubleOrNull() ?: 1000.0

        val filterName = parts.getOrNull(1) ?: DateFilter.Month.name
        val filter = runCatching { DateFilter.valueOf(filterName) }
            .getOrDefault(DateFilter.Month)

        ProgressBarPreferences(targetBudget = budget, selectedFilter = filter)
    }

    suspend fun updateProgressBarPreferences(progressBarSettings: ProgressBarPreferences) {
        dataStore.edit { prefs ->
            val stringToSave = "${progressBarSettings.targetBudget}|${progressBarSettings.selectedFilter.name}"
            prefs[PROGRESS_BAR_SETTINGS_KEY] = stringToSave
        }
    }

    val exchangeRateFlow: Flow<ExchangeRatesPreferences> = dataStore.data.map { prefs ->
        val savedString = prefs[EXCHANGE_RATES_KEY] ?: return@map ExchangeRatesPreferences()

        val parts = savedString.split("|")

        val usdUahExchangeRate = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val eurUahExchangeRate = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val updatedAt = parts.getOrNull(2)?.toLongOrNull() ?: 0L

        ExchangeRatesPreferences(usdUahExchangeRate, eurUahExchangeRate, updatedAt)
    }

    suspend fun updateExchangeRatesPreferences(exchangeRates: ExchangeRatesPreferences) {
        dataStore.edit { prefs ->
            val stringToSave = "${exchangeRates.usdUahExchangeRate}|${exchangeRates.eurUahExchangeRate}|${System.currentTimeMillis()}"
            prefs[EXCHANGE_RATES_KEY] = stringToSave
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

