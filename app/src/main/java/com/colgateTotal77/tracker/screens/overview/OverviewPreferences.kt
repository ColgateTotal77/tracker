package com.colgateTotal77.tracker.screens.overview

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.colgateTotal77.tracker.core.enums.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ProgressBarPreferences(
    var target: Double = 1000.0,
)

@Serializable
data class ExchangeRatesPreferences(
    val exchangeRatesMap: Map<Currency, Double> = buildMap {
        put(Currency.USD, 1.0)
        put(Currency.EUR, 1.0)
    },
    val updatedAt: Long = 0
)

class OverviewPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    private val PROGRESS_BAR_SETTINGS_KEY = stringPreferencesKey("progressBarSettings")
    private val EXCHANGE_RATES_KEY = stringPreferencesKey("exchangeRates")

    val progressBarSettingsFlow: Flow<ProgressBarPreferences> = dataStore.data.map { prefs ->
        val savedString = prefs[PROGRESS_BAR_SETTINGS_KEY]
        if (savedString.isNullOrEmpty()) return@map ProgressBarPreferences()

        try { Json.decodeFromString<ProgressBarPreferences>(savedString) }
        catch (e: Exception) { ProgressBarPreferences() }
    }

    suspend fun updateProgressBarPreferences(progressBarSettings: ProgressBarPreferences) {
        dataStore.edit { prefs ->
            prefs[PROGRESS_BAR_SETTINGS_KEY] = Json.encodeToString(progressBarSettings)
        }
    }

    val exchangeRateFlow: Flow<ExchangeRatesPreferences> = dataStore.data.map { prefs ->
        val savedString = prefs[EXCHANGE_RATES_KEY]
        if (savedString.isNullOrEmpty()) return@map ExchangeRatesPreferences()

        try { Json.decodeFromString<ExchangeRatesPreferences>(savedString) }
        catch (e: Exception) { ExchangeRatesPreferences() }
    }

    suspend fun updateExchangeRatesPreferences(exchangeRates: ExchangeRatesPreferences) {
        dataStore.edit { prefs ->
            val ratesToSave = exchangeRates.copy(updatedAt = System.currentTimeMillis())
            prefs[EXCHANGE_RATES_KEY] = Json.encodeToString(ratesToSave)
        }
    }
}
