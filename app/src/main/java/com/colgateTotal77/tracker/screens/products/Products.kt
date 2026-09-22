package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun Products(
    modifier: Modifier,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory),
) {
    val dimensions = LocalDimensions.current

    val products = viewModel.productFlow.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {  }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { paddingValues ->
        if (products.itemCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensions.screenPadding)
                    .padding(paddingValues),
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
                contentPadding = PaddingValues(bottom = dimensions.screenBottomPadding)
            ) {
                items(
                    count = products.itemCount,
                    key = products.itemKey { it.id }
                ) { index ->
                    val product = products[index] ?: return@items

                    ProductCard(
                        product,
                        onClick = {}
                    )
                }
            }
        }
    }
}
