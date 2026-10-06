package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.transaction.TransactionDraft
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.ui.DateTimeInputField
import com.colgateTotal77.tracker.core.ui.DropdownInput
import com.colgateTotal77.tracker.core.ui.AppToast
import com.colgateTotal77.tracker.core.ui.ToastType
import com.colgateTotal77.tracker.screens.dashboard.Camera.Camera
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    markets: List<MarketEntity>,
    onDismiss: () -> Unit,
    onAdd: (TransactionDraft) -> Unit,
) {
    var dropdownTouched by remember { mutableStateOf(false) }
    var isCameraOpen by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.UAH) }
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

    val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
    var dateTimeInput by remember { mutableStateOf(formatter.format(Date())) }
    val isDateValid = remember(dateTimeInput) {
        if (dateTimeInput.length < 12) false
        else {
            try {
                val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
                formatter.isLenient = false
                formatter.parse(dateTimeInput) != null
            } catch (e: Exception) {
                false
            }
        }
    }

    val dimensions = LocalDimensions.current
    val duplicateMarketMessage = stringResource(R.string.duplicate_name, stringResource(R.string.market))

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = !dropdownTouched,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.add_transaction),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    CustomIconButton(onClick = { isCameraOpen = true }) {
                        Icon(Icons.Default.QrCode2, contentDescription = stringResource(R.string.open_camera))
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filterDecimal() },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Dropdown(
                    onTouchChange = { dropdownTouched = it },
                    items = Currency.entries,
                    selected = currency,
                    onSelect = { selectedCurrency ->
                        selectedCurrency?.let { currency = it }
                    },
                    itemText = { it.dropdownText },
                    itemName = stringResource(R.string.currency),
                    modifier = Modifier.fillMaxWidth(),
                )

                DropdownInput(
                    onTouchChange = { dropdownTouched = it },
                    items = displayMarkets,
                    itemName = stringResource(R.string.market),
                    inputLabel = stringResource(R.string.rename_market),
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
                                ?: MarketEntity(tin = null, name = newName)
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

                DateTimeInputField(
                    value = dateTimeInput,
                    onValueChange = { dateTimeInput = it },
                    isError = dateTimeInput.length == 12 && !isDateValid,
                )

                CustomButton(
                    onClick = {
                        val transactionDraft = TransactionDraft(
                            amountMinor = ((amountInput.toDoubleOrNull() ?: 0.0) * 100).roundToInt(),
                            currency = currency,
                            market = selectedMarket?.let {
                                when {
                                    it.id != 0 -> MarketChoice.Existing(it)
                                    !it.name.isNullOrBlank() -> MarketChoice.New(it.name!!)
                                    else -> MarketChoice.None
                                }
                            } ?: MarketChoice.None,
                            date = formatter.parse(dateTimeInput)!!.time,
                            source = TransactionSource.MANUAL,
                        )

                        onAdd(transactionDraft)
                    },
                    buttonText = stringResource(R.string.save_transaction),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (isCameraOpen) {
        Dialog(
            onDismissRequest = { isCameraOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                Camera(
                    onAdd = onAdd,
                    onClose = { isCameraOpen = false },
                )
            }
        }
    }
}
