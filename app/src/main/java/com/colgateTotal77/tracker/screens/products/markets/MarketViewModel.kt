package com.colgateTotal77.tracker.screens.products.markets

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
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.ToastFailure
import com.colgateTotal77.tracker.core.ui.launchWithToast
import com.colgateTotal77.tracker.core.ui.vico.ChartData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MarketViewModel(
    private val database: AppDatabase,
): ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val marketsFlow: Flow<PagingData<MarketEntity>> = _searchQuery.flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                database.marketDao().query(
                    query = query,
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun insertMarket(market: MarketEntity) {
        launchWithToast(R.string.toast_save_market_failed) {
            val marketDao = database.marketDao()
            val existing = marketDao.getByNormalizedName(market.name.orEmpty().lowercase().trim())
            if (existing != null) throw ToastFailure(R.string.toast_duplicate_market)
            marketDao.insert(market)
        }
    }

    fun updateMarket(market: MarketEntity) {
        launchWithToast(R.string.toast_save_market_failed) {
            val marketDao = database.marketDao()
            val existing = marketDao.getByNormalizedName(market.name.orEmpty().lowercase().trim())
            if (existing != null && existing.id != market.id) throw ToastFailure(R.string.toast_duplicate_market)
            marketDao.update(market)
        }
    }

    fun deleteMarketById(id: Int) {
        launchWithToast(R.string.toast_delete_market_failed, R.string.toast_market_deleted) {
            database.marketDao().deleteById(id)
        }
    }

    fun getMarketHistoryFlow(id: Int): Flow<ChartData> {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")

        return database.marketDao().getHistoryById(id).map { points ->
            ChartData(
                labels = points.map { LocalDate.parse(it.day).format(formatter) },
                values = points.map { (it.amountMinor / 100).toInt() }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TrackerApplication)
                MarketViewModel(
                    application.database,
                )
            }
        }
    }
}