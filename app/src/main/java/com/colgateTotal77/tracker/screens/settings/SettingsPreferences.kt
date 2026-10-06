package com.colgateTotal77.tracker.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.colgateTotal77.tracker.core.enums.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SettingsPref(
    val language: Language = Language.SYSTEM_LANGUAGE,
)

class SettingsPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    private val languageKey = stringPreferencesKey("language")

    val settingsFlow: Flow<SettingsPref> = dataStore.data.map { prefs ->
        val language = prefs[languageKey]
            ?.let { saved -> runCatching { Language.valueOf(saved) }.getOrNull() }
            ?: Language.SYSTEM_LANGUAGE

        SettingsPref(language)
    }

    suspend fun updateSettings(settings: SettingsPref) {
        dataStore.edit { prefs ->
            prefs[languageKey] = settings.language.name
        }
    }
}