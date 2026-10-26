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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.exchange.ExchangeEntity
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductDraft
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductWithProduct
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.enums.OperationId
import com.colgateTotal77.tracker.core.enums.OperationType
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.operations.Exchange.AddExchangeModal
import com.colgateTotal77.tracker.screens.operations.Exchange.DeleteExchangeModal
import com.colgateTotal77.tracker.screens.operations.Exchange.EditExchangeModal
import com.colgateTotal77.tracker.screens.operations.Exchange.ExchangeCard
import com.colgateTotal77.tracker.screens.operations.Expense.AddExpenseModal
import com.colgateTotal77.tracker.screens.operations.Expense.AddExpenseProductModal
import com.colgateTotal77.tracker.screens.operations.Expense.DeleteExpenseModal
import com.colgateTotal77.tracker.screens.operations.Expense.DeleteExpenseProductModal
import com.colgateTotal77.tracker.screens.operations.Expense.EditExpenseModal
import com.colgateTotal77.tracker.screens.operations.Expense.EditExpenseProductModal
import com.colgateTotal77.tracker.screens.operations.Expense.ExpenseCardExpandable
import com.colgateTotal77.tracker.screens.operations.Income.AddIncomeModal
import com.colgateTotal77.tracker.screens.operations.Income.DeleteIncomeModal
import com.colgateTotal77.tracker.screens.operations.Income.EditIncomeModal
import com.colgateTotal77.tracker.screens.operations.Income.IncomeCard
import com.colgateTotal77.tracker.screens.overview.ExchangeRatesPreferences
import com.colgateTotal77.tracker.screens.products.Products
import kotlinx.coroutines.flow.first

@Composable
fun Operations(
    expendedOperationId: OperationId?,
    changeExpendedOperationId: (id: OperationId?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(factory = OperationsViewModel.Factory),
) {
    var isProductsOpen by remember { mutableStateOf(false) }
    if (isProductsOpen) {
        Products(modifier = modifier, onBack = { isProductsOpen = false })
        return
    }

    var isAddMenuOpen by remember { mutableStateOf(false) }
    var operationToAdd by remember { mutableStateOf<OperationType?>(null) }
    var isEditExpenseModalOpen by remember { mutableStateOf(false) }
    var isDeleteExpenseModalOpen by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var isEditIncomeModalOpen by remember { mutableStateOf(false) }
    var isDeleteIncomeModalOpen by remember { mutableStateOf(false) }
    var selectedIncome by remember { mutableStateOf<IncomeEntity?>(null) }

    var isEditExchangeModalOpen by remember { mutableStateOf(false) }
    var isDeleteExchangeModalOpen by remember { mutableStateOf(false) }
    var selectedExchange by remember { mutableStateOf<ExchangeEntity?>(null) }

    var isAddExpenseProductModalOpen by remember { mutableStateOf(false) }
    var isEditExpenseProductModalOpen by remember { mutableStateOf(false) }
    var isDeleteExpenseProductModalOpen by remember { mutableStateOf(false) }
    var selectedExpenseProduct by remember { mutableStateOf<ExpenseProductWithProduct?>(null) }
    var nextExpenseProductPosition by remember { mutableIntStateOf(0) }

    val dimensions = LocalDimensions.current

    val operations = viewModel.operationsFlow.collectAsLazyPagingItems()
    val markets by viewModel.marketsFlow.collectAsStateWithLifecycle()
    val products by viewModel.productFlow.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsState()

    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle(
        initialValue = ExchangeRatesPreferences(),
    )

    val listState = rememberLazyListState()
    LaunchedEffect(expendedOperationId) {
        if (expendedOperationId == null) return@LaunchedEffect
        val index = viewModel.getOperationPosition(expendedOperationId.id, expendedOperationId.type) ?: return@LaunchedEffect

        snapshotFlow { operations.itemCount }
            .first { it > index }

        val targetKey = "${expendedOperationId.type}:${expendedOperationId.id}"
        if (!listState.layoutInfo.visibleItemsInfo.any { it.key == targetKey }) {
            listState.scrollToItem(index)
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Box(modifier = Modifier.size(56.dp)) {
                if (!isAddMenuOpen) {
                    FloatingActionButton(
                        onClick = { isAddMenuOpen = true },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_operation))
                    }
                } else {
                    Popup(
                        alignment = Alignment.BottomEnd,
                        onDismissRequest = { isAddMenuOpen = false },
                        properties = PopupProperties(focusable = true),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
                        ) {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.exchange)) },
                                icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) },
                                onClick = {
                                    operationToAdd = OperationType.EXCHANGE
                                    isAddMenuOpen = false
                                },
                            )
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.income)) },
                                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = {
                                    operationToAdd = OperationType.INCOME
                                    isAddMenuOpen = false
                                },
                            )
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.expense)) },
                                icon = { Icon(Icons.Default.Remove, contentDescription = null) },
                                onClick = {
                                    operationToAdd = OperationType.EXPENSE
                                    isAddMenuOpen = false
                                },
                            )
                        }
                    }
                }
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
                onValueChange = { viewModel.onSearchQueryChanged(it) },
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

            if (operations.itemCount == 0) {
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
                        count = operations.itemCount,
                        key = operations.itemKey {
                            "${it.operation.type}:${it.operation.expenseId ?: it.operation.incomeId ?: it.operation.exchangeId}"
                        },
                    ) { index ->
                        val operation = operations[index]

                        if (operation == null) {
                            Box(Modifier.fillMaxWidth().height(80.dp))
                            return@items
                        }

                        when (operation.operation.type) {
                            OperationType.EXPENSE -> {
                                val expenseItem = operation.expense ?: return@items
                                val operationId = OperationId(
                                    id = expenseItem.expense.id,
                                    type = OperationType.EXPENSE,
                                )
                                val isExpanded = expendedOperationId == operationId

                                ExpenseCardExpandable(
                                    expenseItem = expenseItem,
                                    isExpanded = isExpanded,
                                    onExpandedChange = {
                                        if (isExpanded) changeExpendedOperationId(null)
                                        else changeExpendedOperationId(operationId)
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
                            OperationType.INCOME -> {
                                val income = operation.income ?: return@items
                                IncomeCard(
                                    income = income,
                                    onEdit = {
                                        selectedIncome = income
                                        isEditIncomeModalOpen = true
                                    },
                                    onDelete = {
                                        selectedIncome = income
                                        isDeleteIncomeModalOpen = true
                                    },
                                )
                            }

                            OperationType.EXCHANGE -> {
                                val exchange = operation.exchange ?: return@items

                                ExchangeCard(
                                    exchange = exchange,
                                    onEdit = {
                                        selectedExchange = exchange
                                        isEditExchangeModalOpen = true
                                    },
                                    onDelete = {
                                        selectedExchange = exchange
                                        isDeleteExchangeModalOpen = true
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        when (operationToAdd) {
            OperationType.EXPENSE -> AddExpenseModal(
                markets = markets,
                onDismiss = { operationToAdd = null },
                onAdd = { expense ->
                    viewModel.addExpense(expense) { id ->
                        changeExpendedOperationId(
                            OperationId(
                                id = id,
                                type = OperationType.EXPENSE
                            )
                        )
                        operationToAdd = null
                    }
                },
            )
            OperationType.INCOME -> AddIncomeModal(
                onDismiss = { operationToAdd = null },
                onAdd = { amountMinor, currency, date ->
                    viewModel.addIncome(amountMinor, currency, date) { id ->
                        changeExpendedOperationId(OperationId(id = id, type = OperationType.INCOME))
                        operationToAdd = null
                    }
                },
            )
            OperationType.EXCHANGE -> AddExchangeModal(
                exchangeRates = exchangeRates,
                onDismiss = { operationToAdd = null },
                onAdd = { fromAmountMinor, fromCurrency, toAmountMinor, toCurrency, date ->
                    viewModel.addExchange(fromAmountMinor, fromCurrency, toAmountMinor, toCurrency, date) { id ->
                        changeExpendedOperationId(OperationId(id = id, type = OperationType.EXCHANGE))
                        operationToAdd = null
                    }
                },
            )
            null -> Unit
        }

        if (isEditExpenseModalOpen) {
            EditExpenseModal(
                expense = selectedExpense!!,
                markets = markets,
                onDismiss = { isEditExpenseModalOpen = false },
                onUpdate = { amountMinor, currency, market, date ->
                    viewModel.updateExpense(
                        selectedExpense!!.copy(
                            amountMinor = amountMinor,
                            currency = currency,
                            date = date,
                            updatedAt = System.currentTimeMillis(),
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

        if (isEditIncomeModalOpen) {
            EditIncomeModal(
                income = selectedIncome!!,
                onDismiss = { isEditIncomeModalOpen = false },
                onUpdate = { amountMinor, currency, date ->
                    viewModel.updateIncome(
                        selectedIncome!!.copy(
                            amountMinor = amountMinor,
                            currency = currency,
                            date = date,
                            note = "",
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                    isEditIncomeModalOpen = false
                },
            )
        }

        if (isDeleteIncomeModalOpen) {
            DeleteIncomeModal(
                onDismiss = { isDeleteIncomeModalOpen = false },
                onDelete = {
                    viewModel.deleteIncomeById(selectedIncome!!.id)
                    isDeleteIncomeModalOpen = false
                },
            )
        }

        if (isEditExchangeModalOpen) {
            EditExchangeModal(
                exchange = selectedExchange!!,
                exchangeRates = exchangeRates,
                onDismiss = { isEditExchangeModalOpen = false },
                onUpdate = { fromAmountMinor, fromCurrency, toAmountMinor, toCurrency, date ->
                    viewModel.updateExchange(
                        selectedExchange!!.copy(
                            fromAmountMinor = fromAmountMinor,
                            fromCurrency = fromCurrency,
                            toAmountMinor = toAmountMinor,
                            toCurrency = toCurrency,
                            date = date,
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                    isEditExchangeModalOpen = false
                },
            )
        }

        if (isDeleteExchangeModalOpen) {
            DeleteExchangeModal(
                onDismiss = { isDeleteExchangeModalOpen = false },
                onDelete = {
                    viewModel.deleteExchangeById(selectedExchange!!.id)
                    isDeleteExchangeModalOpen = false
                },
            )
        }
    }
}
