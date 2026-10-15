package com.colgateTotal77.tracker.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.OperationId
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.ProgressBar
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.settings.Settings

@Composable
fun Overview(
    onOperationClick: (id: OperationId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = viewModel(factory = OverviewViewModel.Factory),
) {
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    if (isSettingsOpen) {
        Settings(modifier = modifier, onBack = { isSettingsOpen = false })
        return
    }

    val dimensions = LocalDimensions.current

    val operations by viewModel.recentOperations.collectAsStateWithLifecycle()

    val progressState by viewModel.progressBarState.collectAsStateWithLifecycle()
    val settings = progressState.settings
    val spending = progressState.currentSpending

    val balance by viewModel.balance.collectAsStateWithLifecycle()
    var isAdjustBalanceModalOpen by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.screenPadding)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProgressBar(
                        progressState = progressState,
                        label = stringResource(R.string.budget, stringResource(settings.selectedFilter.labelRes), formatMoney(spending), formatMoney(progressState.targetForFilter)),
                        color =
                            if (spending > progressState.targetForFilter) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        updateProgressBarPreferences = { settings ->
                            viewModel.updateProgressBarPreferences(settings)
                        },
                    )
                }
                CustomIconButton(onClick = { isSettingsOpen = true }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.tab_settings),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            CardWrapper(
                modifier = Modifier
                    .fillMaxWidth(1f / 3f)
                    .aspectRatio(1f),
                contentPadding = PaddingValues(0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(dimensions.cornerRadius))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { isAdjustBalanceModalOpen = true }
                        .padding(dimensions.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall, Alignment.CenterVertically),
                ) {
                    Text(
                        text = stringResource(R.string.available_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = formatMoney(balance),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            if (operations.count() == 0) {
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
                        Text(stringResource(R.string.no_operations_yet), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.add_first_operation))
                    }
                }
            } else {
                Column(modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(dimensions.cornerRadius))
                ) {
                    operations.forEach { operation ->
                        operation.expense?.let {
                            key("${operation.operation.type}:${it.expense.id}") {
                                ExpenseCard(
                                    it,
                                    onClick = {onOperationClick(
                                        OperationId(
                                            id = it.expense.id,
                                            type = operation.operation.type
                                        )
                                    )}
                                )
                            }
                        }
                        operation.income?.let {
                            key("${operation.operation.type}:${it.id}") {
                                IncomeCard(
                                    it,
                                    onClick = {onOperationClick(
                                        OperationId(
                                            id = it.id,
                                            type = operation.operation.type
                                        )
                                    )}
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isAdjustBalanceModalOpen) {
            AdjustBalanceModal(
                currentBalance = balance,
                onDismiss = { isAdjustBalanceModalOpen = false },
                onAdd = { adjustmentAmountMinor ->
                    viewModel.insertBalanceAdjustment(adjustmentAmountMinor)
                    isAdjustBalanceModalOpen = false
                },
            )
        }
    }
}
