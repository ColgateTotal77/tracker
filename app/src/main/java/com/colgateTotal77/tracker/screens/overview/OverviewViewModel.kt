package com.colgateTotal77.tracker.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.colgateTotal77.tracker.TrackerApplication
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.launchWithToast
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.colgateTotal77.tracker.core.database.AppDatabase
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.IncomeType
import com.colgateTotal77.tracker.core.enums.toTimeRange
import com.colgateTotal77.tracker.core.ui.ProgressBarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class OverviewViewModel(
    private val database: AppDatabase,
    private val preferencesRepository: OverviewPreferences,
) : ViewModel() {

    val recentOperations = database.operationDao().getRecent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val progressBarState: StateFlow<ProgressBarState> = preferencesRepository.progressBarSettingsFlow
        .flatMapLatest { settings ->
            val range = settings.selectedFilter.toTimeRange()
            val spendingFlow = database.expenseDao().getTotalSpendingFlow(range.start, range.end)
            spendingFlow.map { spendingAmount ->
                ProgressBarState(
                    settings = settings,
                    currentSpending = spendingAmount,
                    targetForFilter = calculateTargetBudget(
                        settings.selectedFilter,
                        settings.targetBudget
                    )
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProgressBarState()
        )

    fun updateProgressBarPreferences(settings: ProgressBarPreferences) {
        launchWithToast(R.string.toast_save_settings_failed) {
            preferencesRepository.updateProgressBarPreferences(settings)
        }
    }

    val balance = database.operationDao().getBalance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0,
        )

    fun insertBalanceAdjustment(currentBalance: Int) {
        launchWithToast(R.string.toast_save_balance_failed) {
            database.incomeDao().insert(IncomeEntity(
                amountMinor = currentBalance,
                currency = Currency.UAH,
                note = "",
                type = IncomeType.BALANCE_ADJUSTMENT,
                date = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TrackerApplication)
                OverviewViewModel(
                    application.database,
                    application.overviewPreferencesRepository,
                )
            }
        }
    }
}