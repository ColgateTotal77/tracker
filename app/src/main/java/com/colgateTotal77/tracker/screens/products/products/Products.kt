package com.colgateTotal77.tracker.screens.products.products

import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.core.ui.OneActionModal
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ProductNameNormalizer
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.enums.ProductSort
import com.colgateTotal77.tracker.core.enums.TimeRange
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.DropdownPopup
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.ui.theme.addPadding
import com.colgateTotal77.tracker.screens.products.markets.Markets

@Composable
fun Products(
    isFilterModalOpen: Boolean,
    setIsFilterModalOpen: (isFilterModalOpen: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory),
) {
    var isAddProductModalOpen by remember { mutableStateOf(false) }
    var isEditProductModalOpen by remember { mutableStateOf(false) }
    var isArchiveProductModalOpen by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val dimensions = LocalDimensions.current

    val markets by viewModel.marketsFlow.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    val products = viewModel.productFlow.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .padding(bottom = dimensions.navigationBarHeightAndPadding)
                        .size(56.dp)
                ) {
                    FloatingActionButton(onClick = { isAddProductModalOpen = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_product)
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
                if (products.loadState.refresh is LoadState.NotLoading && products.itemCount == 0) return@Scaffold Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = dimensions.searchBarHeightAndPadding * 2 + dimensions.segmentedButtonHeight,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    ) {
                        Icon(Icons.Default.ProductionQuantityLimits, contentDescription = null)
                        Text(
                            stringResource(R.string.no_products_yet),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(stringResource(R.string.add_first_product))
                    }
                }
            }

            LazyColumn(
                contentPadding = paddingValues.addPadding(
                    top = dimensions.searchBarHeightAndPadding + dimensions.segmentedButtonHeight,
                    bottom = dimensions.screenBottomPadding + dimensions.navigationBarHeightAndPadding
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                items(
                    count = products.itemCount,
                    key = products.itemKey { it.product.id }
                ) { index ->
                    val productFromQuery = products[index] ?: return@items

                    ProductCard(
                        productFromQuery = productFromQuery,
                        onGetProductHistory = { id ->
                            viewModel.getProductHistoryFlow(id)
                        },
                        onEdit = {
                            selectedProduct = productFromQuery.product
                            isEditProductModalOpen = true
                        },
                        onArchive = {
                            selectedProduct = productFromQuery.product
                            isArchiveProductModalOpen = true
                        }
                    )
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
            OneActionModal(
                title = stringResource(R.string.archive_product),
                mainText = stringResource(R.string.confirm_archive_product, selectedProduct!!.alias),
                actionButtonText = stringResource(R.string.archive_product),
                onDismiss = { isArchiveProductModalOpen = false },
                onAction = {
                    viewModel.archiveProductById(selectedProduct!!.id)
                    isArchiveProductModalOpen = false
                }
            )
        }

        if (isFilterModalOpen) {
            FilterModal(
                markets = markets,
                filters = filters,
                onDismiss = { setIsFilterModalOpen(false) },
                onApply = { newFilters ->
                    viewModel.onFiltersChanged(newFilters)
                    setIsFilterModalOpen(false)
                },
            )
        }
    }
}
