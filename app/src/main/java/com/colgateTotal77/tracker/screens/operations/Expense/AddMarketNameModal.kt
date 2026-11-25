package com.colgateTotal77.tracker.screens.operations.Expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.DropdownInput
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun AddMarketNameModal(
    markets: List<MarketEntity>,
    onDismiss: () -> Unit,
    onSave: (market: MarketChoice) -> Unit,
) {
    val dimensions = LocalDimensions.current

    var selectedMarket by remember { mutableStateOf<MarketEntity?>(null) }
    var isMarketInputOpen by remember { mutableStateOf(false) }

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

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenHorizontalPadding)) {
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
                        val now = System.currentTimeMillis()

                        selectedMarket = markets.find {
                            it.name.orEmpty().trim().equals(newName, ignoreCase = true)
                        } ?: MarketEntity(
                            name = newName,
                            createdAt = now,
                            updatedAt = now,
                        )
                        true
                    },
                    isInputOpen = isMarketInputOpen,
                    onIsInputOpenChange = { isMarketInputOpen = it },
                    additionalBottomContent = {
                        CustomButton(
                            onClick = { isMarketInputOpen = true },
                            buttonText = stringResource(R.string.add_market),
                            icon = Icons.Rounded.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                    CustomButton(
                        onClick = {
                            val market = selectedMarket!!
                            onSave(if (market.id != 0) MarketChoice.Existing(market) else MarketChoice.New(market.name!!))
                        },
                        enabled = !isMarketInputOpen && !selectedMarket?.name.isNullOrBlank(),
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