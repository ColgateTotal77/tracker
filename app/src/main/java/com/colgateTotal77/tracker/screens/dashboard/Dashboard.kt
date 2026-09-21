package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.enums.TransactionStatus
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.ui.ProgressBar
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt

@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    var isAddTransactionModalOpen by remember { mutableStateOf(false) }
    var isTransactionModalOpen by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val dimensions = LocalDimensions.current

    val transactions = viewModel.transactionsFlow.collectAsLazyPagingItems()
    val target by viewModel.budgetState.collectAsStateWithLifecycle()
    val markets by viewModel.marketsFlow.collectAsStateWithLifecycle()

    val spent by remember {
        derivedStateOf {
            transactions.itemSnapshotList.items.sumOf { it.amountMinor / 100.0 }
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddTransactionModalOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.screenPadding)
                .padding(paddingValues),
        ) {
            ProgressBar(
                current = spent,
                target = target,
                label = "Budget: ${formatMoney(spent.roundToInt())} / ${formatMoney(target.roundToInt())}",
                color = if (spent <= target) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                onUpdateTarget = { newBudget -> viewModel.updateBudget(newBudget) },
            )

            if (transactions.itemCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Default.ReceiptLong, contentDescription = null)
                        Text("No transactions yet", style = MaterialTheme.typography.bodyLarge)
                        Text("Tap + to add your first transaction")
                    }
                }
            } else {
                LazyColumn {
                    items(
                        count = transactions.itemCount,
                        key = transactions.itemKey { it.id },
                    ) { index ->
                        val transaction = transactions[index] ?: return@items
                        TransactionCard(
                            transaction = transaction,
                            onClick = {
                                selectedTransaction = transaction
                                isTransactionModalOpen = true
                            },
                        )
                    }
                }
            }
        }

        if (isAddTransactionModalOpen) {
            AddTransactionModal(
                markets = markets,
                onDismiss = { isAddTransactionModalOpen = false },
                onAdd = { transaction ->
                    viewModel.addTransaction(transaction)
                    isAddTransactionModalOpen = false
                },
            )
        }

        selectedTransaction?.let { transaction ->
            if (isTransactionModalOpen) {
                TransactionModal(
                    transaction = transaction,
                    onDismiss = { isTransactionModalOpen = false },
                    onUpdate = { amountMinor, currency, date ->
                        isTransactionModalOpen = false
                        viewModel.updateTransaction(
                            transaction.copy(
                                amountMinor = amountMinor,
                                currency = currency,
                                date = date ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    },
                    onDelete = {
                        isTransactionModalOpen = false
                        viewModel.deleteTransactionById(transaction.id)
                    },
                )
            }
        }
    }
}