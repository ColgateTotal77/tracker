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
import com.colgateTotal77.tracker.core.database.product.ProductDao
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.enums.ProductSort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


class ProductViewModel(
    private val productDao: ProductDao,
): ViewModel() {
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(ProductSort.PURCHASE_COUNT_DESC)
    val sortBy: StateFlow<ProductSort> = _sortBy.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val productFlow: Flow<PagingData<ProductEntity>> = combine(
        _searchQuery,
        _sortBy
    ) { query, sort ->
        Pair(query, sort)
    }.flatMapLatest { (query, sort) ->
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                productDao.query(
                    nameQuery = query,
                    sortBy = sort
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query.takeIf { it.isNotBlank() }
    }

    fun onSortChanged(sort: ProductSort) {
        _sortBy.value = sort
    }

    fun insertProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.insertOrRestore(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.update(product)
        }
    }

    fun archiveProductById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.archiveById(id)
        }
    }

    fun restoreProductById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.restoreById(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TrackerApplication)
                ProductViewModel(
                    application.database.productDao(),
                )
            }
        }
    }
}