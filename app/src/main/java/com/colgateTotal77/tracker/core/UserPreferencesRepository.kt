package com.colgateTotal77.tracker.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val BUDGET_KEY = doublePreferencesKey("monthly_budget")

    val budgetFlow: Flow<Double> = dataStore.data
        .map { prefs ->
            prefs[BUDGET_KEY] ?: 1000.0
        }

    suspend fun updateBudget(newBudget: Double) {
        dataStore.edit { prefs ->
            prefs[BUDGET_KEY] = newBudget
        }
    }
}