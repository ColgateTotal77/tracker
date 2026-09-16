package com.colgateTotal77.tracker.screens.dashboard

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
import com.colgateTotal77.tracker.core.UserPreferencesRepository
import com.colgateTotal77.tracker.core.database.transaction.TransactionDao
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val transactionDao: TransactionDao,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val transactionsFlow: Flow<PagingData<TransactionEntity>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = transactionDao::query,
    ).flow.cachedIn(viewModelScope)

    val budgetState: StateFlow<Double> = preferencesRepository.budgetFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1000.0,
        )

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insert(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.update(transaction)
        }
    }

    fun deleteTransactionById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteById(id)
        }
    }

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.updateBudget(newBudget)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TrackerApplication)
                DashboardViewModel(
                    application.database.transactionDao(),
                    application.userPreferencesRepository,
                )
            }
        }
    }
}