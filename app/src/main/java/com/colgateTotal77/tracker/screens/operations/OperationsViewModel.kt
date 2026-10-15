package com.colgateTotal77.tracker.screens.operations

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
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.IncomeType
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
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductEntity
import com.colgateTotal77.tracker.core.enums.ExpenseStatus
import androidx.paging.map
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.product.ProductChoice
import com.colgateTotal77.tracker.core.database.expense.ExpenseDraft
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductDraft
import com.colgateTotal77.tracker.core.database.operation.OperationWithDetails
import kotlinx.coroutines.flow.map

class OperationsViewModel(private val database: AppDatabase) : ViewModel() {

    val operationsFlow: Flow<PagingData<OperationWithDetails>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true
        ),
        pagingSourceFactory = database.operationDao()::query,
    ).flow.map { pagingData ->
        pagingData.map { operationItem ->
            operationItem.copy(
                expense = operationItem.expense?.let { expense ->
                    expense.copy(
                        items = expense.items.sortedBy {
                            it.expenseProduct.position
                        }
                    )
                }
            )
        }
    }.cachedIn(viewModelScope)

    fun addExpense(expense: ExpenseDraft, onCallback: (id: Int) -> Unit) {
        launchWithToast(
            error = R.string.toast_save_expense_failed,
            success = R.string.toast_receipt_added.takeIf { expense.source == ExpenseSource.QR_CODE },
        ) {
            val expenseId =  database.withTransaction {
                val marketDao = database.marketDao()
                val productDao = database.productDao()
                val now = System.currentTimeMillis()

                val marketId: Int? = when (val market = expense.market) {
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

                val expenseId = database.expenseDao().insert(
                    ExpenseEntity(
                        amountMinor = expense.amountMinor,
                        currency = expense.currency,
                        status = ExpenseStatus.DONE,
                        source = expense.source,
                        marketId = marketId,
                        fiscalId = expense.fiscalId,
                        note = expense.note,
                        date = expense.date,
                        rawFiscalPayload = expense.rawFiscalPayload,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
                if (expenseId == -1L) {
                    if(expense.fiscalId != null) {
                        val expense = database.expenseDao().getTransactionByFiscalId(expense.fiscalId);
                        onCallback(expense.id)
                    }
                    throw ToastFailure(R.string.toast_duplicate_receipt)
                }

                val distinctProducts = expense.items.mapNotNull { item ->
                    val name = item.name ?: return@mapNotNull null
                    val normalizedName = ProductNameNormalizer.normalize(name)
                    normalizedName to item
                }.toMap()
                if (distinctProducts.isEmpty()) return@withTransaction expenseId.toInt()

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

                val expenseProducts = expense.items.mapIndexedNotNull { index, item ->
                    val name = item.name ?: return@mapIndexedNotNull null
                    val normalizedName = ProductNameNormalizer.normalize(name)

                    val productId = productIdMap[normalizedName] ?: return@mapIndexedNotNull null

                    ExpenseProductEntity(
                        expenseId = expenseId.toInt(),
                        productId = productId,
                        quantity = item.quantity,
                        unitPriceMinor = item.unitPriceMinor,
                        totalMinor = item.totalMinor ?: (item.unitPriceMinor * item.quantity),
                        position = item.position ?: index,
                        isManuallyCreated = false,
                        barcode = item.barcode
                    )
                }

                if (expenseProducts.isEmpty()) return@withTransaction expenseId.toInt()

                database.expenseProductDao().insertAll(expenseProducts)

                expenseId.toInt()
            }

            onCallback(expenseId)
        }
    }

    fun addIncome(amountMinor: Int, currency: Currency, date: Long, onSaved: (Int) -> Unit) {
        launchWithToast(R.string.toast_save_income_failed) {
            val now = System.currentTimeMillis()
            val id = database.incomeDao().insert(IncomeEntity(
                amountMinor = amountMinor,
                currency = currency,
                note = "",
                type = IncomeType.INCOME,
                date = date,
                createdAt = now,
                updatedAt = now,
            ))
            if (id == -1L) throw ToastFailure(R.string.toast_save_income_failed)
            onSaved(id.toInt())
        }
    }

    fun updateIncome(income: IncomeEntity) {
        launchWithToast(R.string.toast_save_income_failed) {
            database.incomeDao().update(income)
        }
    }

    fun deleteIncomeById(id: Int) {
        launchWithToast(R.string.toast_delete_income_failed, R.string.toast_income_deleted) {
            database.incomeDao().deleteById(id)
        }
    }

    fun updateExpense(expense: ExpenseEntity, market: MarketChoice) {
        launchWithToast(R.string.toast_save_expense_failed) {
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
                database.expenseDao().update(expense.copy(marketId = marketId))
            }
        }
    }

    fun deleteExpenseById(id: Int) {
        launchWithToast(R.string.toast_delete_expense_failed, R.string.toast_expense_deleted) {
            database.expenseDao().deleteById(id)
        }
    }

    val marketsFlow: StateFlow<List<MarketEntity>> = database.marketDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExpenseProduct(expenseProduct: ExpenseProductDraft, productChoice: ProductChoice) {
        launchWithToast(R.string.toast_save_expense_product_failed) {
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
                                lastPrice = expenseProduct.unitPriceMinor,
                                createdAt = now,
                                updatedAt = now,
                            )
                        )

                        productDao.getByNormalizedName(normalizedName)?.id
                            ?: throw ToastFailure(R.string.toast_save_expense_product_failed)
                    }
                }

                val totalMinor = expenseProduct.unitPriceMinor * (expenseProduct.quantity / 1000)

                database.expenseProductDao().insert(
                    ExpenseProductEntity(
                        expenseId = expenseProduct.expenseId,
                        productId = productId,
                        quantity = expenseProduct.quantity,
                        unitPriceMinor = expenseProduct.unitPriceMinor,
                        totalMinor = totalMinor,
                        position = expenseProduct.position,
                        isManuallyCreated = expenseProduct.isManuallyCreated
                    )
                )

                database.expenseDao().bumpAmountMinorById(expenseProduct.expenseId, totalMinor, now)
            }
        }
    }

    fun updateExpenseProduct(expenseProduct: ExpenseProductEntity, alias: String) {
        launchWithToast(R.string.toast_save_expense_product_failed) {
            database.withTransaction {
                database.expenseProductDao().update(expenseProduct)
                database.productDao().updateAlliesById(expenseProduct.productId, alias)
            }
        }
    }

    fun deleteExpenseProductById(id: Int) {
        launchWithToast(R.string.toast_delete_expense_product_failed, R.string.toast_expense_product_deleted) {
            database.expenseProductDao().deleteById(id)
        }
    }

    val productFlow: StateFlow<List<ProductEntity>> = database.productDao().getAllActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getOperationPosition(
        expenseId: Int? = null,
        incomeId: Int? = null
    ): Int = database.operationDao().getPosition(expenseId, incomeId)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TrackerApplication)
                OperationsViewModel(application.database)
            }
        }
    }
}