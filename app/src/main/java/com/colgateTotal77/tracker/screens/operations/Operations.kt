package com.colgateTotal77.tracker.screens.operations

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductDraft
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductWithProduct
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.products.Products
import kotlinx.coroutines.flow.first

@Composable
fun Operations(
    expendedExpenseId: Int?,
    changeExpendedExpenseId: (id: Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(factory = OperationsViewModel.Factory),
) {
    var isProductsOpen by remember { mutableStateOf(false) }
    if (isProductsOpen) {
        Products(modifier = modifier, onBack = { isProductsOpen = false })
        return
    }

    var isAddExpenseModalOpen by remember { mutableStateOf(false) }
    var isEditExpenseModalOpen by remember { mutableStateOf(false) }
    var isDeleteExpenseModalOpen by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var isAddExpenseProductModalOpen by remember { mutableStateOf(false) }
    var isEditExpenseProductModalOpen by remember { mutableStateOf(false) }
    var isDeleteExpenseProductModalOpen by remember { mutableStateOf(false) }
    var selectedExpenseProduct by remember { mutableStateOf<ExpenseProductWithProduct?>(null) }
    var nextExpenseProductPosition by remember { mutableIntStateOf(0) }

    val dimensions = LocalDimensions.current

    val expenses = viewModel.expensesFlow.collectAsLazyPagingItems()
    val markets by viewModel.marketsFlow.collectAsStateWithLifecycle()
    val products by viewModel.productFlow.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(expendedExpenseId) {
        val id = expendedExpenseId ?: return@LaunchedEffect
        val index = viewModel.getExpensePosition(id)

        snapshotFlow { expenses.itemCount }
            .first { it > index }

        listState.scrollToItem(index)
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddExpenseModalOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_expense))
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.screenPadding)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_operations)) },
                singleLine = true,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VerticalDivider(modifier = Modifier.height(dimensions.verticalDividerHeight))
                        CustomIconButton(onClick = { isProductsOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = stringResource(R.string.tab_products),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )

            if (expenses.itemCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    ) {
                        Icon(Icons.AutoMirrored.Default.ReceiptLong, contentDescription = null)
                        Text(
                            stringResource(R.string.no_operations_yet),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(stringResource(R.string.add_first_operation))
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = dimensions.screenBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
                ) {
                    items(
                        count = expenses.itemCount,
                        key = expenses.itemKey { it.expense.id },
                    ) { index ->
                        val expenseItem = expenses[index]
                        val isExpended = expendedExpenseId == expenseItem?.expense?.id;

                        if (expenseItem == null) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                            )
                        } else {
                            ExpenseCardExpandable(
                                expenseItem = expenseItem,
                                expanded = expendedExpenseId == expenseItem.expense.id,
                                onExpandedChange = {
                                    if (isExpended) changeExpendedExpenseId(null)
                                    else changeExpendedExpenseId(expenseItem.expense.id)
                                },
                                onEdit = {
                                    selectedExpense = expenseItem.expense
                                    isEditExpenseModalOpen = true
                                },
                                onDelete = {
                                    selectedExpense = expenseItem.expense
                                    isDeleteExpenseModalOpen = true
                                },
                                onAddExpenseProduct = { nextPosition ->
                                    selectedExpense = expenseItem.expense
                                    nextExpenseProductPosition = nextPosition
                                    isAddExpenseProductModalOpen = true
                                },
                                onEditExpenseProduct = { expenseProduct ->
                                    selectedExpenseProduct = expenseProduct
                                    isEditExpenseProductModalOpen = true
                                },
                                onDeleteExpenseProduct = { expenseProduct ->
                                    selectedExpenseProduct = expenseProduct
                                    isDeleteExpenseProductModalOpen = true
                                },
                            )
                        }
                    }
                }
            }
        }

        if (isAddExpenseModalOpen) {
            AddExpenseModal(
                markets = markets,
                onDismiss = { isAddExpenseModalOpen = false },
                onAdd = { expense ->
                    viewModel.addExpense(
                        expense
                    ) { id ->
                        changeExpendedExpenseId(id)
                    }
                    isAddExpenseModalOpen = false
                },
            )
        }

        if (isEditExpenseModalOpen) {
            EditExpenseModal(
                expense = selectedExpense!!,
                markets = markets,
                onDismiss = { isEditExpenseModalOpen = false },
                onUpdate = { amountMinor, currency, market, date ->
                    val now = System.currentTimeMillis()

                    viewModel.updateExpense(
                        selectedExpense!!.copy(
                            amountMinor = amountMinor,
                            currency = currency,
                            date = date ?: now,
                            updatedAt = now,
                        ),
                        market = market,
                    )
                    isEditExpenseModalOpen = false
                },
            )
        }

        if (isDeleteExpenseModalOpen) {
            DeleteExpenseModal(
                onDismiss = { isDeleteExpenseModalOpen = false },
                onDelete = {
                    viewModel.deleteExpenseById(selectedExpense!!.id)
                    isDeleteExpenseModalOpen = false
                }
            )
        }

        if (isAddExpenseProductModalOpen) {
            AddExpenseProductModal(
                products = products,
                onDismiss = { isAddExpenseProductModalOpen = false },
                onAdd = { productChoice, quantity, unitPriceMinor ->
                    viewModel.addExpenseProduct(
                        ExpenseProductDraft(
                            expenseId = selectedExpense!!.id,
                            position = nextExpenseProductPosition,
                            quantity = quantity,
                            unitPriceMinor = unitPriceMinor,
                        ),
                        productChoice
                    )
                    isAddExpenseProductModalOpen = false
                },
            )
        }

        if (isEditExpenseProductModalOpen) {
            EditExpenseProductModal(
                expenseProductWithProduct = selectedExpenseProduct!!,
                onDismiss = { isEditExpenseProductModalOpen = false },
                onUpdate = { alias, quantity, unitPriceMinor ->
                    viewModel.updateExpenseProduct(
                        selectedExpenseProduct!!.expenseProduct.copy(
                            quantity = quantity,
                            unitPriceMinor = unitPriceMinor,
                            totalMinor = unitPriceMinor * (quantity / 1000)
                        ),
                        alias
                    )
                    isEditExpenseProductModalOpen = false
                },
            )
        }

        if (isDeleteExpenseProductModalOpen) {
            DeleteExpenseProductModal(
                expenseProduct = selectedExpenseProduct!!,
                onDismiss = { isDeleteExpenseProductModalOpen = false },
                onDelete = {
                    viewModel.deleteExpenseProductById(selectedExpenseProduct!!.expenseProduct.id)
                    isDeleteExpenseProductModalOpen = false
                }
            )
        }
    }
}
