package com.colgateTotal77.tracker.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.colgateTotal77.tracker.TrackerApplication
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.launchWithToast
import com.colgateTotal77.tracker.core.enums.Language
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: SettingsPreferences,
) : ViewModel() {

    init {
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collect { applyLanguage(it.language) }
        }
    }

    val settings = preferencesRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsPref(),
    )

    fun updateSettings(setting: SettingsPref) {
        launchWithToast(R.string.toast_save_settings_failed) {
            preferencesRepository.updateSettings(setting)
        }
    }

    fun applyLanguage(language: Language) {
        val locales = when (language) {
            Language.SYSTEM_LANGUAGE -> LocaleListCompat.getEmptyLocaleList()
            Language.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            Language.UKRAINIAN -> LocaleListCompat.forLanguageTags("uk")
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TrackerApplication
                SettingsViewModel(application.settingsPreferencesRepository)
            }
        }
    }
}