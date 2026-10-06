package com.colgateTotal77.tracker.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.colgateTotal77.tracker.TrackerApplication
import com.colgateTotal77.tracker.core.database.AppDatabase
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.product.PricePoint
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.database.product.ProductFromQuery
import com.colgateTotal77.tracker.core.enums.ProductSort
import com.colgateTotal77.tracker.core.enums.toTimeRange
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.AppToast
import com.colgateTotal77.tracker.core.ui.ToastFailure
import com.colgateTotal77.tracker.core.ui.launchWithToast
import androidx.room.withTransaction
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn


class ProductViewModel(
    private val database: AppDatabase,
): ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(ProductSort.PURCHASE_COUNT_DESC)
    val sortBy: StateFlow<ProductSort> = _sortBy.asStateFlow()

    private val _filters = MutableStateFlow(ProductFilters())
    val filters: StateFlow<ProductFilters> = _filters.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val productFlow: Flow<PagingData<ProductFromQuery>> = combine(
        _searchQuery,
        _sortBy,
        _filters,
    ) { query, sort, filters ->
        Triple(query, sort, filters)
    }.flatMapLatest { (query, sort, filters) ->
        val productDao = database.productDao()
        val range = filters.date.toTimeRange()
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                productDao.query(
                    nameQuery = query,
                    sortBy = sort,
                    startTime = range.start,
                    endTime = range.end,
                    minPrice = filters.minPrice,
                    maxPrice = filters.maxPrice,
                    marketIds = filters.marketIds.toList().ifEmpty { null },
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortChanged(sort: ProductSort) {
        _sortBy.value = sort
    }

    fun onFiltersChanged(filters: ProductFilters) {
        _filters.value = filters
    }

    fun clearFilters() {
        _filters.value = ProductFilters()
    }

    fun insertProduct(product: ProductEntity) {
        launchWithToast(R.string.toast_save_product_failed) {
            val restored = database.withTransaction {
                val dao = database.productDao()
                val existing = dao.getByNormalizedName(product.normalizedName)
                if (existing != null && !existing.isArchived) {
                    throw ToastFailure(R.string.toast_duplicate_product)
                }
                dao.insertOrRestore(product)
                existing != null
            }
            if (restored) withContext(Dispatchers.Main) { AppToast.show(R.string.toast_product_restored) }
        }
    }

    fun updateProduct(product: ProductEntity) {
        launchWithToast(R.string.toast_save_product_failed) {
            database.productDao().update(product)
        }
    }

    fun archiveProductById(id: Int) {
        launchWithToast(R.string.toast_archive_product_failed, R.string.toast_product_archived) {
            database.productDao().archiveById(id)
        }
    }

    fun restoreProductById(id: Int) {
        launchWithToast(R.string.toast_restore_product_failed, R.string.toast_product_restored) {
            database.productDao().restoreById(id)
        }
    }

    fun getProductHistoryFlow(id: Int): Flow<List<PricePoint>> {
        return database.productDao().getHistoryById(id)
    }

    val marketsFlow: StateFlow<List<MarketEntity>> = database.marketDao().getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TrackerApplication)
                ProductViewModel(
                    application.database,
                )
            }
        }
    }
}