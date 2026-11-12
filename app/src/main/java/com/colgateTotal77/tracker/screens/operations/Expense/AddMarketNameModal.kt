package com.colgateTotal77.tracker.screens.operations.Expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.ui.AppToast
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.DropdownInput
import com.colgateTotal77.tracker.core.ui.ToastType
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun AddMarketNameModal(
    markets: List<MarketEntity>,
    onDismiss: () -> Unit,
    onSave: (market: MarketChoice) -> Unit,
) {
    val dimensions = LocalDimensions.current

    var selectedMarket by remember { mutableStateOf<MarketEntity?>(null) }

    val displayMarkets = remember(markets, selectedMarket) {
        val updatedMarkets = markets.map {
            if (it.id == selectedMarket?.id) selectedMarket!! else it
        }

        val validMarkets = updatedMarkets.filter {
            it.id == selectedMarket?.id || !it.name.isNullOrBlank()
        }

        if (selectedMarket?.id == 0 && updatedMarkets.none { it.id == 0 }) {
            validMarkets + selectedMarket!!
        } else validMarkets
    }

    val duplicateMarketMessage = stringResource(R.string.duplicate_name, stringResource(R.string.market))

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    stringResource(R.string.add_market_name),
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    stringResource(R.string.add_market_name_description),
                    style = MaterialTheme.typography.bodyMedium,
                )

                DropdownInput(
                    items = displayMarkets,
                    itemName = stringResource(R.string.market),
                    inputLabel = stringResource(R.string.market_name),
                    itemText = { it?.name ?: "" },
                    selected = selectedMarket,
                    onSelect = { selectedMarket = it },
                    modifier = Modifier.fillMaxWidth(),
                    onInputDone = { newName ->
                        val duplicate = markets.any {
                            it.id != selectedMarket?.id &&
                                    it.name.orEmpty().trim().equals(newName, ignoreCase = true)
                        }
                        if (duplicate) {
                            AppToast.show(duplicateMarketMessage, ToastType.Error)
                            false
                        } else {
                            selectedMarket = selectedMarket?.copy(name = newName)
                                ?: MarketEntity(name = newName)
                            true
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedMarket == null) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = stringResource(
                                if (selectedMarket == null) R.string.create_market else R.string.rename_market
                            )
                        )
                    },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                    CustomButton(
                        onClick = {
                            val market = selectedMarket!!
                            onSave(if (market.id != 0) MarketChoice.Existing(market) else MarketChoice.New(market.name!!))
                        },
                        enabled = selectedMarket != null && !selectedMarket?.name.isNullOrBlank(),
                        buttonText = stringResource(R.string.save_market_name),
                        modifier = Modifier.weight(1f),
                    )

                    CustomButton(
                        onClick = onDismiss,
                        buttonText = stringResource(R.string.cancel),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}