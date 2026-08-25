package com.colgateTotal77.tracker.screens.dashboard

import android.icu.number.Precision.currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.ui.ProgressBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MMM.yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    var isAddExpenseModalOpen by remember { mutableStateOf(false) }
    val expenses by viewModel.expensesState.collectAsStateWithLifecycle()

    val spent by remember(expenses) {
        derivedStateOf {
            expenses
                .filter { it.timestamp <= System.currentTimeMillis() }
                .sumOf { it.amount }
        }
    }

    val budget = 1000.0

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddExpenseModalOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            ProgressBar(
                spent,
                budget,
                "Budget: $spent / $budget",
                color = if (spent > budget) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(expenses) { expense ->
                    Row(
                        modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = expense.category,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = formatTimestamp(expense.timestamp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            text = "-${expense.amount} ${expense.currency}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            if (isAddExpenseModalOpen) {
                AddExpenseModal(
                    onDismiss = { isAddExpenseModalOpen = false },
                    onAdd = { amount: Double, category: String ->
                        isAddExpenseModalOpen = false
                        val expense = ExpenseEntity(
                            amount = amount,
                            category = category,
                            currency = "₴",
                        )
                        viewModel.addExpense(expense)
                    }
                )
            }
        }
    }
}