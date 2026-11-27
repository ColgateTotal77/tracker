package com.colgateTotal77.tracker.screens.operations

import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.core.ui.OneActionModal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.exchange.ExchangeEntity
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductDraft
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductWithProduct
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.enums.OperationId
import com.colgateTotal77.tracker.core.enums.OperationType
import com.colgateTotal77.tracker.core.ui.Toolbar
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.ui.theme.addPadding
import com.colgateTotal77.tracker.screens.operations.Exchange.AddExchangeModal
import com.colgateTotal77.tracker.screens.operations.Exchange.EditExchangeModal
import com.colgateTotal77.tracker.screens.operations.Exchange.ExchangeCard
import com.colgateTotal77.tracker.screens.operations.Expense.AddExpenseModal
import com.colgateTotal77.tracker.screens.operations.Expense.AddExpenseProductModal
import com.colgateTotal77.tracker.screens.operations.Expense.AddMarketNameModal
import com.colgateTotal77.tracker.screens.operations.Expense.EditExpenseModal
import com.colgateTotal77.tracker.screens.operations.Expense.EditExpenseProductModal
import com.colgateTotal77.tracker.screens.operations.Expense.ExpenseCardExpandable
import com.colgateTotal77.tracker.screens.operations.Income.AddIncomeModal
import com.colgateTotal77.tracker.screens.operations.Income.EditIncomeModal
import com.colgateTotal77.tracker.screens.operations.Income.IncomeCard
import com.colgateTotal77.tracker.screens.overview.ExchangeRatesPreferences
import kotlinx.coroutines.flow.first

@Composable
fun Operations(
    modifier: Modifier = Modifier,
    viewModel: OperationsViewModel = viewModel(factory = OperationsViewModel.Factory),
) {
    var isAddMenuOpen by remember { mutableStateOf(false) }
    var operationToAdd by remember { mutableStateOf<OperationType?>(null) }
    var isAddMarketNameModalOpen by remember { mutableStateOf(false) }
    var isEditExpenseModalOpen by remember { mutableStateOf(false) }
    var isDeleteExpenseModalOpen by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var marketToName by remember { mutableStateOf<MarketEntity?>(null) }

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
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val expendedId by viewModel.expendedId.collectAsStateWithLifecycle()

    var isFilterModalOpen by remember { mutableStateOf(false) }

    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle(
        initialValue = ExchangeRatesPreferences(),
    )

    val timeRange by viewModel.timeRange.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(filters, searchQuery, timeRange) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(expendedId) {
        if (expendedId == null) return@LaunchedEffect
        val index = viewModel.getOperationPosition(expendedId!!.id, expendedId!!.type) ?: return@LaunchedEffect

        snapshotFlow { operations.itemCount }
            .first { it > index }

        if (!listState.layoutInfo.visibleItemsInfo.any { it.index == index }) {
            listState.scrollToItem(index)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = dimensions.navigationBarHeightAndPadding)
                        .size(56.dp)
                ) {
                    if (!isAddMenuOpen) {
                        FloatingActionButton(
                            onClick = { isAddMenuOpen = true },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_operation)
                            )
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
                                    icon = {
                                        Icon(
                                            Icons.Default.CurrencyExchange,
                                            contentDescription = null
                                        )
                                    },
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
                                    icon = {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = null
                                        )
                                    },
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
            if (operations.loadState.refresh is LoadState.NotLoading && operations.itemCount == 0) return@Scaffold Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensions.screenHorizontalPadding,
                        end = dimensions.screenHorizontalPadding,
                        top = dimensions.searchBarHeightAndPadding
                    ),
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

            LazyColumn(
                state = listState,
                modifier = Modifier.padding( horizontal = dimensions.screenHorizontalPadding),
                contentPadding = paddingValues.addPadding(
                    top = dimensions.searchBarHeightAndPadding,
                    bottom = dimensions.screenBottomPadding + dimensions.navigationBarHeightAndPadding
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            ) {
                items(count = operations.itemCount) { index ->
                    val operation = operations[index]

                    if (operation == null) {
                        Box(Modifier
                            .fillMaxWidth()
                            .height(80.dp))
                        return@items
                    }

                    when (operation.operation.type) {
                        OperationType.EXPENSE -> {
                            val expenseItem = operation.expense ?: return@items
                            val operationId = OperationId(
                                id = expenseItem.expense.id,
                                type = OperationType.EXPENSE,
                            )
                            val isExpanded = expendedId == operationId

                            ExpenseCardExpandable(
                                expenseItem = expenseItem,
                                isExpanded = isExpanded,
                                onExpandedChange = {
                                    if (isExpanded) viewModel.onExpendedIdChanged(null)
                                    else viewModel.onExpendedIdChanged(operationId)
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

        Toolbar(
            searchQuery = searchQuery,
            onSearchChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = stringResource(R.string.search_operations),
            timeRange = timeRange,
            onTimeRangeChange = { viewModel.onTimeRangeChanged(it) },
            onFilterClick = { isFilterModalOpen = true },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = dimensions.screenHorizontalPadding,
                    vertical = dimensions.spacingSmall
                ),
        )

        when (operationToAdd) {
            OperationType.EXPENSE -> AddExpenseModal(
                markets = markets,
                onDismiss = { operationToAdd = null },
                onAdd = { expense ->
                    viewModel.addExpense(expense) { id, market ->
                        viewModel.onExpendedIdChanged(
                            OperationId(
                                id = id,
                                type = OperationType.EXPENSE
                            )
                        )
                        operationToAdd = null

                        if (market != null && market.name.isNullOrBlank()) {
                            marketToName = market
                            isAddMarketNameModalOpen = true
                        }
                    }
                },
            )

            OperationType.INCOME -> AddIncomeModal(
                onDismiss = { operationToAdd = null },
                onAdd = { amountMinor, currency, date ->
                    viewModel.addIncome(amountMinor, currency, date) { id ->
                        viewModel.onExpendedIdChanged(
                            OperationId(
                                id = id,
                                type = OperationType.INCOME
                            )
                        )
                        operationToAdd = null
                    }
                },
            )

            OperationType.EXCHANGE -> AddExchangeModal(
                exchangeRates = exchangeRates,
                onDismiss = { operationToAdd = null },
                onAdd = { fromAmountMinor, fromCurrency, toAmountMinor, toCurrency, date ->
                    viewModel.addExchange(
                        fromAmountMinor,
                        fromCurrency,
                        toAmountMinor,
                        toCurrency,
                        date
                    ) { id ->
                        viewModel.onExpendedIdChanged(
                            OperationId(
                                id = id,
                                type = OperationType.EXCHANGE
                            )
                        )
                        operationToAdd = null
                    }
                },
            )

            null -> Unit
        }

        if (isAddMarketNameModalOpen) {
            AddMarketNameModal(
                markets = markets,
                onDismiss = {
                    marketToName = null
                    isAddMarketNameModalOpen = false
                },
                onSave = { choice ->
                    when (choice) {
                        is MarketChoice.Existing -> viewModel.mergeMarket(
                            marketToName!!.id,
                            choice.market
                        )

                        is MarketChoice.New -> viewModel.renameMarket(marketToName!!.copy(name = choice.name.trim()))
                        else -> Unit
                    }
                    marketToName = null
                    isAddMarketNameModalOpen = false
                }
            )
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
            OneActionModal(
                title = stringResource(R.string.delete_expense),
                mainText = stringResource(R.string.confirm_delete_expense),
                actionButtonText = stringResource(R.string.delete_expense),
                onDismiss = { isDeleteExpenseModalOpen = false },
                onAction = {
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
            OneActionModal(
                title = stringResource(R.string.delete_product),
                mainText = stringResource(R.string.confirm_delete_expense_product, selectedExpenseProduct!!.product.alias),
                actionButtonText = stringResource(R.string.delete_product),
                onDismiss = { isDeleteExpenseProductModalOpen = false },
                onAction = {
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
            OneActionModal(
                title = stringResource(R.string.delete_income),
                mainText = stringResource(R.string.confirm_delete_income),
                actionButtonText = stringResource(R.string.delete_income),
                onDismiss = { isDeleteIncomeModalOpen = false },
                onAction = {
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
            OneActionModal(
                title = stringResource(R.string.delete_exchange),
                mainText = stringResource(R.string.confirm_delete_exchange),
                actionButtonText = stringResource(R.string.delete_exchange),
                onDismiss = { isDeleteExchangeModalOpen = false },
                onAction = {
                    viewModel.deleteExchangeById(selectedExchange!!.id)
                    isDeleteExchangeModalOpen = false
                },
            )
        }

        if (isFilterModalOpen) {
            FilterModal(
                markets = markets,
                filters = filters,
                onDismiss = { isFilterModalOpen = false },
                onApply = { newFilters ->
                    viewModel.onFiltersChanged(newFilters)
                    isFilterModalOpen = false
                },
            )
        }
    }
}