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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.core.ProductNameNormalizer
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.enums.DateFilter
import com.colgateTotal77.tracker.core.enums.ProductSort
import com.colgateTotal77.tracker.core.ui.DropdownPopup
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Products(
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory),
) {
    var isAddProductModalOpen by remember { mutableStateOf(false) }
    var isEditProductModalOpen by remember { mutableStateOf(false) }
    var isArchiveProductModalOpen by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val dimensions = LocalDimensions.current

    val products = viewModel.productFlow.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.sortBy.collectAsState()
    val currentDateFilter by viewModel.dateFilter.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddProductModalOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.screenPadding)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search products...") },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
                ) {
                    DropdownPopup(
                        items = ProductSort.entries,
                        selected = currentSort,
                        onSelect = { selectedSort ->
                            selectedSort?.let { viewModel.onSortChanged(it) }
                        },
                        itemText = { sort -> sort.label },
                        itemName = "Sort",
                        modifier = Modifier.weight(1f),
                    )

                    DropdownPopup(
                        items = DateFilter.entries,
                        selected = currentDateFilter,
                        onSelect = { selectedDateFilter ->
                            selectedDateFilter?.let { viewModel.onDateFilterChange(it) }
                        },
                        itemText = { dateFilter -> dateFilter.label },
                        itemName = "Date filter",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (products.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing),
                    ) {
                        Icon(Icons.Default.ProductionQuantityLimits, contentDescription = null)
                        Text("No Product yet", style = MaterialTheme.typography.bodyLarge)
                        Text("Tap + to add your first transaction")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = dimensions.screenBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
                ) {
                    items(
                        count = products.itemCount,
                        key = products.itemKey { it.id }
                    ) { index ->
                        val product = products[index] ?: return@items

                        ProductCard(
                            product = product,
                            onGetProductHistory = { id ->
                                viewModel.getProductHistoryFlow(id)
                            },
                            onEdit = {
                                selectedProduct = product
                                isEditProductModalOpen = true
                            },
                            onArchive = {
                                selectedProduct = product
                                isArchiveProductModalOpen = true
                            }
                        )
                    }
                }
            }
        }

        if (isAddProductModalOpen) {
            AddProductModal(
                onDismiss = { isAddProductModalOpen = false },
                onAdd = { name ->
                    val now = System.currentTimeMillis()

                    viewModel.insertProduct(
                        ProductEntity(
                            normalizedName = ProductNameNormalizer.normalize(name),
                            alias = name,
                            lastPrice = 0,
                            createdAt = now,
                            updatedAt = now,
                        )
                    )
                    isAddProductModalOpen = false
                },
            )
        }

        if (isEditProductModalOpen) {
            EditProductModal(
                product = selectedProduct!!,
                onDismiss = { isEditProductModalOpen = false },
                onUpdate = { alias ->
                    viewModel.updateProduct(
                        selectedProduct!!.copy(
                            alias = alias
                        )
                    )
                    isEditProductModalOpen = false
                },
            )
        }

        if (isArchiveProductModalOpen) {
            ArchiveProductModal(
                product = selectedProduct!!,
                onDismiss = { isArchiveProductModalOpen = false },
                onArchive = { id ->
                    viewModel.archiveProductById(id)
                    isArchiveProductModalOpen = false
                }
            )
        }
    }
}