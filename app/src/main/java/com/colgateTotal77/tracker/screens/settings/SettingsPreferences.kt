package com.colgateTotal77.tracker.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.colgateTotal77.tracker.core.enums.Language
import com.colgateTotal77.tracker.core.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SettingsPref(
    val language: Language = Language.SYSTEM_LANGUAGE,
    val theme: ThemeMode = ThemeMode.System
)

class SettingsPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    private val SETTINGS_KEY = stringPreferencesKey("settings")
    val settingsFlow: Flow<SettingsPref> = dataStore.data.map { prefs ->
        val savedString = prefs[SETTINGS_KEY]
        if (savedString.isNullOrEmpty()) return@map SettingsPref()

        try { Json.decodeFromString<SettingsPref>(savedString) }
        catch (e: Exception) { SettingsPref() }
    }

    suspend fun updateSettings(settings: SettingsPref) {
        dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = Json.encodeToString(settings)
        }
    }
}