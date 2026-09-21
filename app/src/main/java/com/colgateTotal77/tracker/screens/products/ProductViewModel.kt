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
import com.colgateTotal77.tracker.core.UserPreferencesRepository
import com.colgateTotal77.tracker.core.database.product.ProductDao
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class ProductViewModel(
    private val productDao: ProductDao,
): ViewModel() {
    val productFlow: Flow<PagingData<ProductEntity>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = productDao::query,
    ).flow.cachedIn(viewModelScope)

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