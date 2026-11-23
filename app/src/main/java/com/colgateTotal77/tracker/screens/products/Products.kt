package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.ProductSort
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.DropdownPopup
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.products.markets.MarketViewModel
import com.colgateTotal77.tracker.screens.products.markets.Markets
import com.colgateTotal77.tracker.screens.products.products.ProductViewModel
import com.colgateTotal77.tracker.screens.products.products.Products

@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory),
    marketViewModel: MarketViewModel = viewModel(factory = MarketViewModel.Factory)
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val options = listOf(R.string.tab_products, R.string.markets)

    val dimensions = LocalDimensions.current

    val searchQuery by (if (selected == 0) productViewModel.searchQuery else marketViewModel.searchQuery).collectAsStateWithLifecycle()
    val currentSort by productViewModel.sortBy.collectAsState()
    var isFilterModalOpen by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        if (selected == 0) Products(
            isFilterModalOpen,
            {it: Boolean -> isFilterModalOpen = it },
            viewModel = productViewModel
        )
        else Markets(viewModel = marketViewModel)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    horizontal = dimensions.screenHorizontalPadding,
                    vertical = dimensions.spacingSmall
                ),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = selected == index,
                            onClick = { selected = index },
                            shape = SegmentedButtonDefaults.itemShape(
                                index,
                                options.size,
                                RoundedCornerShape(dimensions.cornerRadius)
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                            )
                        ) {
                            Text(stringResource(label))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { if (selected == 0) productViewModel.onSearchQueryChanged(it) else marketViewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(dimensions.cornerRadius))
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        RoundedCornerShape(dimensions.cornerRadius)
                    ),
                placeholder = { Text(stringResource(if (selected == 0) R.string.search_products else R.string.search_markets)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                ),
                trailingIcon = {
                    if (selected == 0) Row(verticalAlignment = Alignment.CenterVertically) {
                        VerticalDivider(modifier = Modifier.height(dimensions.verticalDividerHeight))
                        CustomIconButton(onClick = { isFilterModalOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = stringResource(R.string.filter),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )

            if (selected == 0) {
                DropdownPopup(
                    items = ProductSort.entries,
                    selected = currentSort,
                    onSelect = { selectedSort ->
                        selectedSort?.let { productViewModel.onSortChanged(it) }
                    },
                    itemText = { sort -> stringResource(sort.labelRes) },
                    itemName = stringResource(R.string.sort),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(dimensions.cornerRadius))
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                            RoundedCornerShape(dimensions.cornerRadius)
                        ),
                )
            }
        }
    }
}