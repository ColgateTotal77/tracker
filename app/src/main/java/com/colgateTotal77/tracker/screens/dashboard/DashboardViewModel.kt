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
import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.FiscalCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.room.withTransaction
import com.colgateTotal77.tracker.core.database.AppDatabase
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductEntity
import com.colgateTotal77.tracker.core.enums.TransactionStatus

class DashboardViewModel(
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val transactionsFlow: Flow<PagingData<TransactionEntity>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = database.transactionDao()::query,
    ).flow.cachedIn(viewModelScope)

    val budgetState: StateFlow<Double> = preferencesRepository.budgetFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1000.0,
        )

    val marketsFlow: StateFlow<List<MarketEntity>> = database.marketDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(transaction: TransactionDraft) {
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                val transactionDao = database.transactionDao()
                val marketDao = database.marketDao()
                val productDao = database.productDao()
                val transactionProductDao = database.transactionProductDao()
                val now = System.currentTimeMillis()

                val marketId: Int? = when (val market = transaction.market) {
                    is MarketChoice.Existing -> market.marketId

                    is MarketChoice.ByTin -> {
                        val existing = marketDao.getByTin(market.tin)
                        existing?.id ?: run {
                            val insertId = marketDao.insert(MarketEntity(tin = market.tin, name = ""))
                            if (insertId != -1L) insertId.toInt()
                            else marketDao.getByTin(market.tin)?.id
                        }
                    }

                    is MarketChoice.New -> {
                        val normalizedName = market.name.lowercase().trim()
                        val existing = marketDao.getByNormalizedName(normalizedName)
                        existing?.id ?: run {
                            val insertId = marketDao.insert(MarketEntity(tin = null, name = normalizedName))
                            if (insertId != -1L) insertId.toInt()
                            else marketDao.getByNormalizedName(normalizedName)?.id
                        }
                    }

                    MarketChoice.None -> null
                }

                val transactionId = transactionDao.insert(
                    TransactionEntity(
                        amountMinor = transaction.amountMinor,
                        currency = transaction.currency,
                        status = TransactionStatus.DONE,
                        source = transaction.source,
                        marketId = marketId,
                        fiscalId = transaction.fiscalId,
                        note = transaction.note,
                        date = transaction.date,
                        rawFiscalPayload = transaction.rawFiscalPayload,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
                if (transactionId == -1L) return@withTransaction //need to add toast (already added)

                val distinctProducts = transaction.items.mapNotNull { item ->
                    val name = item.name ?: return@mapNotNull null
                    val normalizedName = name.lowercase().replace(" ", "")
                    normalizedName to item
                }.toMap()
                if (distinctProducts.isEmpty()) return@withTransaction

                val newProducts = distinctProducts.map { (normalizedName, item) ->
                    ProductEntity(
                        normalizedName = normalizedName,
                        alias = item.name!!,
                        lastPrice = item.unitPriceMinor,
                        barcode = item.barcode,
                        createdAt = now,
                        updatedAt = now
                    )
                }
                productDao.insertAllOrRestore(newProducts)

                val productNames = distinctProducts.keys.toList()
                val fetchedProducts = productDao.getByNormalizedNames(productNames)

                val productIdMap = fetchedProducts.associate { it.normalizedName to it.id }

                val transactionProducts = transaction.items.mapIndexedNotNull { index, item ->
                    val name = item.name ?: return@mapIndexedNotNull null
                    val normalizedName = name.lowercase().replace(" ", "")

                    val productId = productIdMap[normalizedName] ?: return@mapIndexedNotNull null

                    TransactionProductEntity(
                        transactionId = transactionId.toInt(),
                        productId = productId,
                        quantity = item.quantity,
                        unitPriceMinor = item.unitPriceMinor,
                        totalMinor = item.totalMinor ?: (item.unitPriceMinor * item.quantity),
                        taxGroup = item.taxGroup,
                        position = item.position ?: index
                    )
                }

                if (transactionProducts.isEmpty()) return@withTransaction

                transactionProductDao.insertAll(transactionProducts)
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.transactionDao().update(transaction)
        }
    }

    fun deleteTransactionById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            database.transactionDao().deleteById(id)
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
                    application.database,
                    application.userPreferencesRepository,
                )
            }
        }
    }
}