package com.colgateTotal77.tracker.screens.products

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.enums.DateFilter
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.DropdownMultiSelect
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

data class ProductFilters(
    val date: DateFilter = DateFilter.AllTime,
    val marketIds: Set<Int> = emptySet(),
    val maxPrice: Int? = null,
    val minPrice: Int? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterModal(
    markets: List<MarketEntity>,
    filters: ProductFilters,
    onDismiss: () -> Unit,
    onApply: (filters: ProductFilters) -> Unit,
) {
    var dropdownTouched by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(filters.date) }
    var selectedMarketIds by remember { mutableStateOf(filters.marketIds) }
    var minPrice by remember { mutableStateOf(filters.minPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(filters.maxPrice?.toString() ?: "") }

    val displayMarkets = markets.filter { !it.name.isNullOrBlank() }
    val dimensions = LocalDimensions.current
    val context = LocalContext.current

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = !dropdownTouched,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.filter_products),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    CustomIconButton(
                        onClick = {
                            date = filters.date
                            selectedMarketIds = filters.marketIds
                            minPrice = filters.minPrice?.toString() ?: ""
                            maxPrice = filters.maxPrice?.toString() ?: ""
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.open_camera))
                    }
                }

                DropdownMultiSelect(
                    onTouchChange = { dropdownTouched = it },
                    items = displayMarkets,
                    selectedKeys = selectedMarketIds,
                    key = { it.id },
                    itemText = { it.name ?: "" },
                    itemName = stringResource(R.string.markets),
                    onSelectionChange = { selectedMarketIds = it },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.elementSpacing)) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it.filterDecimal() },
                        label = { Text(stringResource(R.string.min_price)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it.filterDecimal() },
                        label = { Text(stringResource(R.string.max_price)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Dropdown(
                    onTouchChange = { dropdownTouched = it },
                    items = DateFilter.entries,
                    selected = date,
                    onSelect = { selectedDateFilter ->
                        selectedDateFilter?.let { date = it }
                    },
                    itemText = { dateFilter -> context.getString(dateFilter.labelRes) },
                    itemName = stringResource(R.string.date_filter),
                )

                CustomButton(
                    onClick = {
                        val newFilters = ProductFilters(
                            date = date,
                            marketIds = selectedMarketIds,
                            minPrice = minPrice.toDoubleOrNull()?.times(100)?.toInt(),
                            maxPrice = maxPrice.toDoubleOrNull()?.times(100)?.toInt(),
                        )
                        onApply(newFilters)
                        onDismiss()
                    },
                    buttonText = stringResource(R.string.apply),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
