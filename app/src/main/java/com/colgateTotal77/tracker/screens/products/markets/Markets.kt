package com.colgateTotal77.tracker.screens.products.markets

import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.core.ui.OneActionModal
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.ui.theme.addPadding

@Composable
fun Markets(
    modifier: Modifier = Modifier,
    viewModel: MarketViewModel = viewModel(factory = MarketViewModel.Factory),
) {
    var isAddMarketModalOpen by remember { mutableStateOf(false) }
    var isEditMarketModalOpen by remember { mutableStateOf(false) }
    var isDeleteMarketModalOpen by remember { mutableStateOf(false) }
    var selectedMarket by remember { mutableStateOf<MarketEntity?>(null) }

    val dimensions = LocalDimensions.current

    val markets = viewModel.marketsFlow.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery) {
        listState.scrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .padding(bottom = dimensions.navigationBarHeightAndPadding)
                        .size(56.dp)
                ) {
                    FloatingActionButton(onClick = { isAddMarketModalOpen = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_market)
                        )
                    }
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensions.screenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            ) {
                if (markets.loadState.refresh is LoadState.NotLoading && markets.itemCount == 0) return@Scaffold Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = dimensions.searchBarHeightAndPadding + dimensions.segmentedButtonHeight,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    ) {
                        Icon(Icons.Default.ProductionQuantityLimits, contentDescription = null)
                        Text(
                            stringResource(R.string.no_markets_yet),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(stringResource(R.string.add_first_market))
                    }
                }
            }
            LazyColumn(
                state = listState,
                contentPadding = paddingValues.addPadding(
                    top = dimensions.searchBarHeightAndPadding + dimensions.segmentedButtonHeight,
                    bottom = dimensions.screenBottomPadding + dimensions.navigationBarHeightAndPadding
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                items(count = markets.itemCount) { index ->
                    val market = markets[index] ?: return@items

                    MarketCard(
                        market = market,
                        onGetMarketHistory = { id ->
                            viewModel.getMarketHistoryFlow(id)
                        },
                        onEdit = {
                            selectedMarket = market
                            isEditMarketModalOpen = true
                        },
                        onDelete = {
                            selectedMarket = market
                            isDeleteMarketModalOpen = true
                        }
                    )
                }
            }
        }

        if (isAddMarketModalOpen) {
            AddMarketModal(
                onDismiss = { isAddMarketModalOpen = false },
                onAdd = { name ->
                    val now = System.currentTimeMillis()

                    viewModel.insertMarket(
                        MarketEntity(
                            name = name,
                            updatedAt = now,
                            createdAt = now,
                        )
                    )
                    isAddMarketModalOpen = false
                },
            )
        }

        if (isEditMarketModalOpen) {
            EditMarketModal(
                market = selectedMarket!!,
                onDismiss = { isEditMarketModalOpen = false },
                onUpdate = { name ->
                    viewModel.updateMarket(
                        selectedMarket!!.copy(
                            name = name
                        )
                    )
                    isEditMarketModalOpen = false
                },
            )
        }

        if (isDeleteMarketModalOpen) {
            OneActionModal(
                title = stringResource(R.string.delete_market),
                mainText = stringResource(R.string.delete_market_confirm),
                actionButtonText = stringResource(R.string.delete_market),
                onDismiss = { isDeleteMarketModalOpen = false },
                onAction = {
                    viewModel.deleteMarketById(selectedMarket!!.id)
                    isDeleteMarketModalOpen = false
                }
            )
        }
    }
}
