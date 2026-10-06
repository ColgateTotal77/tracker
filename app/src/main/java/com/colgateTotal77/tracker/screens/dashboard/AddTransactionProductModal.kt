package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.ProductNameNormalizer
import com.colgateTotal77.tracker.core.ui.AppToast
import com.colgateTotal77.tracker.core.ui.ToastType
import com.colgateTotal77.tracker.core.database.product.ProductChoice
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.DropdownInput
import com.colgateTotal77.tracker.core.ui.QuantityInputField
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionProductModal(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAdd: (productChoice: ProductChoice, quantity: Int, unitPriceMinor: Int) -> Unit,
) {
    var dropdownTouched by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var unitPriceInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var quantityInput by remember { mutableStateOf("1") }

    val dimensions = LocalDimensions.current
    val duplicateProductMessage = stringResource(R.string.duplicate_name, stringResource(R.string.product))

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = !dropdownTouched,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                Text(
                    stringResource(R.string.add_product),
                    style = MaterialTheme.typography.titleLarge,
                )

                DropdownInput(
                    onTouchChange = { dropdownTouched = it },
                    items = products,
                    itemName = stringResource(R.string.product),
                    inputLabel = stringResource(R.string.new_product_name),
                    itemText = { it?.alias ?: ""},
                    selected = selectedProduct,
                    onSelect = {
                        selectedProduct = it
                        if (unitPriceInput == "") unitPriceInput = selectedProduct!!.lastPrice.toString()
                   },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedProduct == null) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = stringResource(
                                if (selectedProduct == null) R.string.create_product else R.string.rename_product
                            )
                        )
                    },
                    onInputDone = { newName ->
                        val normalizedName = ProductNameNormalizer.normalize(newName)
                        val duplicate = products.any {
                            it.id != selectedProduct?.id &&
                                (it.normalizedName == normalizedName ||
                                    ProductNameNormalizer.normalize(it.alias) == normalizedName)
                        }
                        if (duplicate) {
                            AppToast.show(duplicateProductMessage, ToastType.Error)
                            false
                        } else {
                            selectedProduct = selectedProduct?.let {
                                it.copy(
                                    alias = newName,
                                    normalizedName = if (it.id == 0) normalizedName else it.normalizedName,
                                )
                            } ?: ProductEntity(
                                    normalizedName = normalizedName,
                                    alias = newName,
                                    lastPrice = 0,
                                    createdAt = 0,
                                    updatedAt = 0,
                                )
                            true
                        }
                    }
                )

                QuantityInputField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = unitPriceInput,
                    onValueChange = { unitPriceInput = it.filterDecimal() },
                    label = { Text(stringResource(R.string.unit_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        if (selectedProduct?.alias.isNullOrBlank()) return@CustomButton

                        val productChoice =
                            if (selectedProduct?.id == 0) ProductChoice.New(selectedProduct!!.alias)
                            else ProductChoice.Existing(selectedProduct!!)

                        val rawQuantity = quantityInput.toDoubleOrNull() ?: return@CustomButton
                        val unitPriceMinor = ((unitPriceInput.toDoubleOrNull() ?: return@CustomButton) * 100).roundToInt()

                        val quantity =
                            if (selectedUnit != MeasureUnit.G) (rawQuantity * 1000).roundToInt()
                            else rawQuantity.roundToInt()

                        if (quantity <= 0 || unitPriceMinor <= 0) return@CustomButton

                        onAdd(productChoice, quantity, unitPriceMinor)
                    },
                    buttonText = stringResource(R.string.save_product),
                    enabled = !selectedProduct?.alias.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
