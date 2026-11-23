package com.colgateTotal77.tracker.screens.products.markets

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun EditMarketModal(
    market: MarketEntity,
    onDismiss: () -> Unit,
    onUpdate: (name: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf(market.name ?: "") }

    val dimensions = LocalDimensions.current

    AppModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    text = market.name.orEmpty().ifBlank { stringResource(R.string.unnamed_market, market.id) },
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.market_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        val name = nameInput.trim()
                        if (name.isNotEmpty()) onUpdate(name)
                    },
                    buttonText = stringResource(R.string.save_market),
                    enabled = nameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
