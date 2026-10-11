package com.colgateTotal77.tracker.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.colgateTotal77.tracker.TrackerApplication
import com.colgateTotal77.tracker.core.ProductNameNormalizer
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.ExpenseSource
import com.colgateTotal77.tracker.core.ui.ToastFailure
import com.colgateTotal77.tracker.core.ui.launchWithToast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.room.withTransaction
import com.colgateTotal77.tracker.core.database.AppDatabase
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.database.expense.ExpenseWithProducts
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductEntity
import com.colgateTotal77.tracker.core.enums.ExpenseStatus
import androidx.paging.map
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.product.ProductChoice
import com.colgateTotal77.tracker.core.database.expense.ExpenseDraft
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductDraft
import com.colgateTotal77.tracker.core.enums.toTimeRange
import com.colgateTotal77.tracker.core.ui.ProgressBarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class OverviewViewModel(
    private val database: AppDatabase,
    private val preferencesRepository: OverviewPreferences,
) : ViewModel() {

    val recentOperations = database.expenseDao().getRecent()
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