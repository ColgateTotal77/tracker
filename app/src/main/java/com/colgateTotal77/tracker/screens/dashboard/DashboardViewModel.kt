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
import com.colgateTotal77.tracker.core.ProductNameNormalizer
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.TransactionSource
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
import com.colgateTotal77.tracker.core.database.transaction.TransactionWithProducts
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductEntity
import com.colgateTotal77.tracker.core.enums.TransactionStatus
import androidx.paging.map
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.product.ProductChoice
import com.colgateTotal77.tracker.core.database.transaction.TransactionDraft
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductDraft
import com.colgateTotal77.tracker.core.enums.toTimeRange
import com.colgateTotal77.tracker.core.ui.ProgressBarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DashboardViewModel(
    private val database: AppDatabase,
    private val preferencesRepository: DashboardPreferences,
) : ViewModel() {

    val transactionsFlow: Flow<PagingData<TransactionWithProducts>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = database.transactionDao()::query,
    ).flow.map { pagingData ->
        pagingData.map { transactionItem ->
            transactionItem.copy(
                items = transactionItem.items.sortedBy { it.transactionProduct.position }
            )
        }
    }.cachedIn(viewModelScope)

    fun addTransaction(transaction: TransactionDraft) {
        launchWithToast(
            error = R.string.toast_save_transaction_failed,
            success = R.string.toast_receipt_added.takeIf { transaction.source == TransactionSource.QR_CODE },
        ) {
            database.withTransaction {
                val marketDao = database.marketDao()
                val productDao = database.productDao()
                val now = System.currentTimeMillis()

                val marketId: Int? = when (val market = transaction.market) {
                    is MarketChoice.Existing -> {
                        marketDao.update(market.market)
                        market.market.id
                    }

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

                    is MarketChoice.None -> null
                }

                val transactionId = database.transactionDao().insert(
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
                if (transactionId == -1L) throw ToastFailure(R.string.toast_duplicate_receipt)

                val distinctProducts = transaction.items.mapNotNull { item ->
                    val name = item.name ?: return@mapNotNull null
                    val normalizedName = ProductNameNormalizer.normalize(name)
                    normalizedName to item
                }.toMap()
                if (distinctProducts.isEmpty()) return@withTransaction

                val newProducts = distinctProducts.map { (normalizedName, item) ->
                    ProductEntity(
                        normalizedName = normalizedName,
                        alias = item.name!!,
                        lastPrice = item.unitPriceMinor,
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
                    val normalizedName = ProductNameNormalizer.normalize(name)

                    val productId = productIdMap[normalizedName] ?: return@mapIndexedNotNull null

                    TransactionProductEntity(
                        transactionId = transactionId.toInt(),
                        productId = productId,
                        quantity = item.quantity,
                        unitPriceMinor = item.unitPriceMinor,
                        totalMinor = item.totalMinor ?: (item.unitPriceMinor * item.quantity),
                        taxGroup = item.taxGroup,
                        position = item.position ?: index,
                        isManuallyCreated = false,
                        barcode = item.barcode
                    )
                }

                if (transactionProducts.isEmpty()) return@withTransaction

                database.transactionProductDao().insertAll(transactionProducts)
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity, market: MarketChoice) {
        launchWithToast(R.string.toast_save_transaction_failed) {
            database.withTransaction {
                val marketDao = database.marketDao()

                val marketId: Int? = when (market) {
                    is MarketChoice.Existing -> {
                        marketDao.update(market.market)
                        market.market.id
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

                    else -> null
                }
                database.transactionDao().update(transaction.copy(marketId = marketId))
            }
        }
    }

    fun deleteTransactionById(id: Int) {
        launchWithToast(R.string.toast_delete_transaction_failed, R.string.toast_transaction_deleted) {
            database.transactionDao().deleteById(id)
        }
    }

    val marketsFlow: StateFlow<List<MarketEntity>> = database.marketDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransactionProduct(transactionProduct: TransactionProductDraft, productChoice: ProductChoice) {
        launchWithToast(R.string.toast_save_transaction_product_failed) {
            val productDao = database.productDao()
            val now = System.currentTimeMillis()

            database.withTransaction {
                val productId = when (productChoice) {
                    is ProductChoice.Existing -> productChoice.product.id
                    is ProductChoice.New -> {
                        val normalizedName = ProductNameNormalizer.normalize(productChoice.alias)

                        productDao.insertOrRestore(
                            ProductEntity(
                                normalizedName = normalizedName,
                                alias = productChoice.alias.trim(),
                                lastPrice = transactionProduct.unitPriceMinor,
                                createdAt = now,
                                updatedAt = now,
                            )
                        )

                        productDao.getByNormalizedName(normalizedName)?.id
                            ?: throw ToastFailure(R.string.toast_save_transaction_product_failed)
                    }
                }

                val totalMinor = transactionProduct.unitPriceMinor * (transactionProduct.quantity / 1000)

                database.transactionProductDao().insert(
                    TransactionProductEntity(
                        transactionId = transactionProduct.transactionId,
                        productId = productId,
                        quantity = transactionProduct.quantity,
                        unitPriceMinor = transactionProduct.unitPriceMinor,
                        totalMinor = totalMinor,
                        taxGroup = null,
                        position = transactionProduct.position,
                        isManuallyCreated = transactionProduct.isManuallyCreated
                    )
                )

                database.transactionDao().bumpAmountMinorById(transactionProduct.transactionId, totalMinor, now)
            }
        }
    }

    fun updateTransactionProduct(transactionProduct: TransactionProductEntity, alias: String) {
        launchWithToast(R.string.toast_save_transaction_product_failed) {
            database.withTransaction {
                database.transactionProductDao().update(transactionProduct)
                database.productDao().updateAlliesById(transactionProduct.productId, alias)
            }
        }
    }

    fun deleteTransactionProductById(id: Int) {
        launchWithToast(R.string.toast_delete_transaction_product_failed, R.string.toast_transaction_product_deleted) {
            database.transactionProductDao().deleteById(id)
        }
    }

    val productFlow: StateFlow<List<ProductEntity>> = database.productDao().getAllActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val progressBarState: StateFlow<ProgressBarState> = preferencesRepository.progressBarSettingsFlow
        .flatMapLatest { settings ->
            val range = settings.selectedFilter.toTimeRange()
            val spendingFlow = database.transactionDao().getTotalSpendingFlow(range.start, range.end)
            spendingFlow.map { spendingAmount ->
                ProgressBarState(
                    settings = settings,
                    currentSpending = spendingAmount,
                    targetForFilter = calculateTargetBudget(settings.selectedFilter, settings.targetBudget)
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
                DashboardViewModel(
                    application.database,
                    application.dashboardPreferencesRepository,
                )
            }
        }
    }
}