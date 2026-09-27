package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.core.enums.ProductSort
import com.colgateTotal77.tracker.core.ui.DropdownPopup
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Products(
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory),
) {
    val dimensions = LocalDimensions.current

    val products = viewModel.productFlow.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.sortBy.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {  }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery ?: "",
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search products...") },
                    singleLine = true
                )

                DropdownPopup(
                    items = ProductSort.entries,
                    selected = currentSort,
                    onSelect = { selectedSort ->
                        selectedSort?.let { viewModel.onSortChanged(it) }
                    },
                    itemText = { sort -> sort.label },
                    itemName = "Sort",
                    modifier = Modifier.width(140.dp)
                )
            }

            if (products.itemCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensions.screenPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ProductionQuantityLimits, contentDescription = null)
                        Text("No Product yet", style = MaterialTheme.typography.bodyLarge)
                        Text("Tap + to add your first transaction")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = dimensions.screenBottomPadding,
                        start = dimensions.screenPadding,
                        end = dimensions.screenPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = products.itemCount,
                        key = products.itemKey { it.id }
                    ) { index ->
                        val product = products[index] ?: return@items

                        ProductCard(
                            product = product,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}