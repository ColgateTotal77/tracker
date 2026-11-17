package com.colgateTotal77.tracker.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.room.withTransaction
import com.colgateTotal77.tracker.TrackerApplication
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.launchWithToast
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.colgateTotal77.tracker.core.database.AppDatabase
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.database.operation.CurrencyBalance
import com.colgateTotal77.tracker.core.enums.DateFilter
import com.colgateTotal77.tracker.core.enums.IncomeType
import com.colgateTotal77.tracker.core.enums.toTimeRange
import com.colgateTotal77.tracker.core.ui.ProgressBarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import kotlin.math.roundToLong

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
            combine(
                spendingFlow,
                preferencesRepository.exchangeRateFlow,
            ) { spending, rates ->
                ProgressBarState(
                    settings = settings,
                    currentSpending = calculateBudget(
                        rates,
                        spending.associate { it.currency to it.amountMinor },
                    ).roundToLong(),
                    targetForFilter = calculateTargetBudget(
                        settings.selectedFilter,
                        settings.targetBudget,
                    ),
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

    val exchangeRates = preferencesRepository.exchangeRateFlow

    val balances = database.operationDao().getBalance().map { balances ->
        balances.associate { it.currency to it.amountMinor }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap(),
    )

    fun insertBalanceAdjustments(adjustments: List<CurrencyBalance>) {
        launchWithToast(R.string.toast_save_balance_failed) {
            val now = System.currentTimeMillis()
            database.withTransaction {
                adjustments.forEach { item ->
                    database.incomeDao().insert(
                        IncomeEntity(
                            amountMinor = item.amountMinor,
                            currency = item.currency,
                            note = "",
                            type = IncomeType.BALANCE_ADJUSTMENT,
                            date = now,
                            createdAt = now,
                            updatedAt = now,
                        )
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val barChartData: StateFlow<ChartData?> = preferencesRepository.progressBarSettingsFlow
        .flatMapLatest { settings ->
            val filter = settings.selectedFilter
            if (filter != DateFilter.Month && filter != DateFilter.PrevMonth) return@flatMapLatest flowOf(null)

            val range = filter.toTimeRange()
            val startInstant = Instant.ofEpochMilli(range.start!!)
            val startZdt = startInstant.atZone(ZoneId.systemDefault())
            val yearMonth = YearMonth.of(startZdt.year, startZdt.month)

            database.expenseDao().getDailySumsFlow(range.start, range.end!!)
                .map { dailySums ->
                    buildWeeklyChartData(yearMonth, dailySums)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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